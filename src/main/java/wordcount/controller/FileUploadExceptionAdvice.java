package wordcount.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class FileUploadExceptionAdvice {
	@Autowired
	private Environment env;

	@ExceptionHandler(MultipartException.class)
	public ModelAndView handleMultipartException(MultipartException exc, HttpServletRequest request,
			HttpServletResponse response) {
		String maxFileSize = env.getProperty("spring.http.multipart.max-file-size");
		String maxRequestSize = env.getProperty("spring.http.multipart.max-request-size");
		ModelAndView modelAndView = new ModelAndView("uploadform");
		
		if ("SizeLimitExceededException".equals(exc.getCause().getCause().getClass().getSimpleName())){
			modelAndView.getModel().put("message", "Selected files are too large! Maximum allowed total size is " + maxRequestSize);
		}
		else {
			modelAndView.getModel().put("message", "File too large! Maximum allowed file size is " + maxFileSize);
		}
		return modelAndView;
	}
}
