package wordcount.controller;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

import wordcount.model.CountingConcurrentSkipListMap;
import wordcount.model.FileModel;

public class WordCountController {
	private static final String PATTERN = "[a-zA-Z]+";
	private static final List<String> SUPPORTED_ENCODINGS = Arrays.asList("CP-1252", "ISO-8859-1", "ANSI");
	private static final List<String> SPLIT_LETTERS = Arrays.asList("g", "n", "u", "z");
	private final List<FileModel> files;
	private Map<String, Integer> wordCountMap = null;
	
	public WordCountController(List<FileModel> files) {
		this.files = files;
	}

	public Map<String, Map<String, Integer>> performCount(int parallelThreads) throws IOException {
		wordCountMap = new CountingConcurrentSkipListMap();
		ExecutorService executor = Executors.newFixedThreadPool(Math.min(parallelThreads, files.size()));
		executeParallel(executor, "validateFile");
		executeParallel(executor, "parseFile");
		executor.shutdown();
		return splitMap(wordCountMap);
	}

	private Map<String, Map<String, Integer>> splitMap(Map<String, Integer> wordCountMap) {
		Map<String, Map<String, Integer>> groupedMap = new HashMap<String, Map<String, Integer>>();
		for (String letter : SPLIT_LETTERS) {
			groupedMap.put(letter, wordCountMap.entrySet() 
			.stream() 
			.filter(map -> map.getKey().startsWith(letter)) 
			.collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue())));
		}
		return groupedMap;
	}

	public void parseFile(FileModel file) {
		System.out.println("Parse input file: " + file.getFileName() + " Thread: " + Thread.currentThread().getName());
		String input = new String(file.getBytes());
		Pattern pattern = Pattern.compile(PATTERN);
		Matcher matcher = pattern.matcher(input);
		while (matcher.find()) {
			String foundWord = input.substring(matcher.start(), matcher.end()).toLowerCase();
			wordCountMap.put(foundWord, 1);
		}
	}

	public void validateFile(FileModel file) {
		System.out
				.println("Validate input file: " + file.getFileName() + " Thread: " + Thread.currentThread().getName());
		if ("".equals(file.getFileName())) {
			throw new RuntimeException("File without a file name submitted. Please select at least one file.");
		}
		if (!"text/plain".equals(file.getContentType())) {
			throw new RuntimeException(
					"Incompatible type of file: " + file.getFileName() + ". Only text files are supported.");
		}
		CharsetDetector charsetDetector = new CharsetDetector();
		charsetDetector.setText(file.getBytes());
		charsetDetector.enableInputFilter(true);
		CharsetMatch cm = charsetDetector.detect();
		if (cm == null || !SUPPORTED_ENCODINGS.contains(cm.getName())) {
			throw new RuntimeException(
					"Incompatible encoding of file: " + file.getFileName() + ". Encoding detected: " + cm.getName());
		}
	}

	private void executeParallel(ExecutorService executor, String methodName) {
		final WordCountController controller = this;
		List<Callable<String>> callables = new ArrayList<Callable<String>>();
		for (FileModel file : files) {
			callables.add(new Callable<String>() {
			    public String call() throws Exception {
			    	Method method = controller.getClass().getMethod(methodName, file.getClass());
			        method.invoke(controller, file);
			    	//validateFile(file);
			        return methodName + " task "+(files.indexOf(file)+1);
			    }
			});
		}
		
		try {
			List<Future<String>> futures = executor.invokeAll(callables);
			for(Future<String> future : futures){
			    System.out.println("future.get = " + future.get());
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt(); // Reset interrupted status
		} catch (ExecutionException e) {
			Throwable exception = e.getCause().getCause(); //also unwrap the InvocationTargetException
			throw new RuntimeException(exception.getMessage());
		}
	}

}
