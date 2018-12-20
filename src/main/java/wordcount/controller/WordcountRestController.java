package wordcount.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import wordcount.model.FileModel;

@Controller
public class WordcountRestController {

	@GetMapping("/")
	public String index() {
		return "uploadform";
	}

	@PostMapping("/")
	public String uploadMultipartFile(@RequestParam("files") MultipartFile[] files, Model model) {
		List<String> fileNames = new ArrayList<String>();
		List<FileModel> uploadedFiles = new ArrayList<FileModel>();
		FileModel fileModel = null;
		try {

			for (MultipartFile file : files) {
				fileModel = new FileModel(file.getOriginalFilename(), file.getContentType(), file.getBytes());
				fileNames.add(file.getOriginalFilename());
				uploadedFiles.add(fileModel);
				System.out.println("File received:" + file.getOriginalFilename());
			}

			model.addAttribute("message", "Files uploaded successfully!");
			model.addAttribute("files", fileNames);

		} catch (Exception e) {
			model.addAttribute("message", "Error occured while uploading files.");
			model.addAttribute("files", fileNames);
			e.printStackTrace();
		}
		if (!uploadedFiles.isEmpty()) {
			WordCountController wordCounter = new WordCountController(uploadedFiles);
			Map<String, Map<String, Integer>> wordCounts = null;
			try {
				wordCounts = wordCounter.performCount(4);
				model.addAttribute("words", wordCounts);
			} catch (IOException e) {
				model.addAttribute("message", "Some error occured while processing your file(s)");
				e.printStackTrace();
			}
			catch (RuntimeException e) {
				model.addAttribute("message", e.getMessage());
				e.printStackTrace();
			}
		}

		return "uploadform";
	}

}
