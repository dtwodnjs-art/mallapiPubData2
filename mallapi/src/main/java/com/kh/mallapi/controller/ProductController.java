package com.kh.mallapi.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kh.mallapi.dto.PageRequestDTO;
import com.kh.mallapi.dto.PageResponseDTO;
import com.kh.mallapi.dto.ProductDTO;
import com.kh.mallapi.service.ProductService;
import com.kh.mallapi.util.CustomFileUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/products")
public class ProductController {
	private final CustomFileUtil fileUtil;
	private final ProductService productService;

	@PostMapping("/")
	public Map<String, Long> register(ProductDTO productDTO) {
		log.info("rgister: " + productDTO);
		// 첨부된파일
		List<MultipartFile> files = productDTO.getFiles();
		// 중복되지않게 파일명을 작성하고, 내부폴더 복사하고, 중복되지 않는 이름을 List<String> 리턴
		List<String> uploadFileNames = fileUtil.saveFiles(files);
		// 업로드된 파일을 파일을 중복되지 않는 파일명을 리스트를 productDTO 저장한다.
		productDTO.setUploadFileNames(uploadFileNames);
		log.info(uploadFileNames);
		// 서비스 호출
		Long pno = productService.register(productDTO);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		return Map.of("result", pno);
	}

	@GetMapping("/view/{fileName}")
	public ResponseEntity<Resource> viewFileGET(@PathVariable String fileName) {
		return fileUtil.getFile(fileName);
	}

	@GetMapping("/list")
	public PageResponseDTO<ProductDTO> list(PageRequestDTO pageRequestDTO) {
		log.info("list............." + pageRequestDTO);
		try { 
			   Thread.sleep(1000); 
			  } catch (InterruptedException e) { 
			   e.printStackTrace(); 
			  } 
		return productService.getList(pageRequestDTO);
	}

	@GetMapping("/{pno}")
	public ProductDTO read(@PathVariable(name = "pno") Long pno) {
		return productService.get(pno);
	}

	@PutMapping("/{pno}")
	public Map<String, String> modify(@PathVariable(name = "pno") Long pno, ProductDTO productDTO) {

		// 1. URL의 경로 변수(pno)를 DTO 객체에 수동으로 세팅 (수정 대상 식별)
		productDTO.setPno(pno);

		// 2. DB에서 수정 전의 원본 상품 데이터를 가져옴 (기존 파일 목록을 확인하기 위함)
		// 예: pno=120, pname="aaa", uploadFileNames=["dsaads_aaaa.jpg"]
		ProductDTO oldProductDTO = productService.get(pno);

		// 3. 수정 전 DB에 저장되어 있던 파일명 리스트를 추출
		// 예: ["dsaads_aaaa.jpg"]
		List<String> oldFileNames = oldProductDTO.getUploadFileNames();

		// 4. 클라이언트가 새로 첨부하여 업로드한 실제 파일 데이터(Binary)를 가져옴
		// 예: 사용자가 bbb.jpg를 새로 선택함
		List<MultipartFile> files = productDTO.getFiles();

		// 5. 신규 파일을 서버 폴더에 물리적으로 저장하고, 생성된 고유 파일명들을 리스트로 반환
		// 예: bbb.jpg 저장 -> "dsaads_bbb.jpg" 반환

		List<String> currentUploadFileNames = null;
		if (files != null && files.get(0).isEmpty()) {
			currentUploadFileNames = fileUtil.saveFiles(files);
		}

		// 6. 클라이언트(화면)에서 삭제하지 않고 그대로 유지하기로 한 기존 파일명 리스트
		// 예: 사용자가 aaaa.jpg는 유지하기로 함 -> ["dsaads_aaaa.jpg"]
		List<String> uploadedFileNames = productDTO.getUploadFileNames();

		// 7. 신규로 업로드된 파일이 있다면, 유지된 파일 목록 뒤에 추가 (최종 저장될 파일 목록 완성)
		if (currentUploadFileNames != null && !currentUploadFileNames.isEmpty()) {
			// 결과 예: ["dsaads_aaaa.jpg", "dsaads_bbb.jpg"]
			uploadedFileNames.addAll(currentUploadFileNames);
		}

		// 8. DB의 상품 정보(이름, 가격 등)와 최종 파일명 리스트를 업데이트
		productService.modify(productDTO);

		// 9. 물리적 파일 삭제 로직: 기존 파일들 중 최종 목록에서 빠진 파일들을 찾아 삭제
		if (oldFileNames != null && !oldFileNames.isEmpty()) {

			// 9-1. 구 파일 목록(oldFileNames) 중 신규 최종 목록(uploadedFileNames)에 없는 것만 필터링
			// 예: 만약 사용자가 aaaa.jpg를 지웠다면, 여기서 "dsaads_aaaa.jpg"가 추출됨
			List<String> removeFiles = oldFileNames.stream()
					.filter(fileName -> uploadedFileNames.indexOf(fileName) == -1).collect(Collectors.toList());

			// 9-2. 추출된 '삭제 대상' 파일들을 서버 로컬 디렉토리에서 실제로 삭제
			fileUtil.deleteFiles(removeFiles);
		}

		// 10. 처리가 성공했음을 알리는 JSON 응답 반환
		return Map.of("RESULT", "SUCCESS");
	}

	@DeleteMapping("/{pno}")
	public Map<String, String> remove(@PathVariable("pno") Long pno) {
		// 삭제해야 할 파일들 알아내기
		List<String> oldFileNames = productService.get(pno).getUploadFileNames();
		//데이블 flag = true update
		productService.remove(pno);
		
		//기존이미지는 삭제함
		fileUtil.deleteFiles(oldFileNames);
		return Map.of("RESULT", "SUCCESS");
	}

}
