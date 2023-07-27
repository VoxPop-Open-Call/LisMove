package net.nextome.lismove.rest;

import net.nextome.lismove.exceptions.LismoveException;
import net.nextome.lismove.integrations.autodata.CarDataService;
import net.nextome.lismove.models.CarBrand;
import net.nextome.lismove.models.CarGeneration;
import net.nextome.lismove.models.CarModel;
import net.nextome.lismove.rest.dto.CarGenerationDto;
import net.nextome.lismove.rest.dto.CarModelDto;
import net.nextome.lismove.rest.dto.CarModificationDto;
import net.nextome.lismove.rest.mappers.CarDataMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("carbrands")
public class CarDataController {
	@Autowired
	private CarDataService carDataService;
	@Autowired
	private CarDataMapper mapper;

	@GetMapping
	public List<CarBrand> getBrands() {
		return carDataService.getBrands();
	}

	@GetMapping("{bid}")
	public CarBrand getBrand(@PathVariable("bid") Long brandId) {
		return carDataService.getBrand(brandId).orElseThrow(() -> new LismoveException("Brand not found", HttpStatus.NOT_FOUND));
	}

	@GetMapping("{bid}/models")
	public List<CarModelDto> getModels(@PathVariable("bid") Long brandId) {
		CarBrand brand = carDataService.getBrand(brandId).orElseThrow(() -> new LismoveException("Brand not found", HttpStatus.NOT_FOUND));
		return mapper.modelToDto(carDataService.getModels(brand));
	}

	@GetMapping("{bid}/models/{mid}")
	public CarModelDto getModel(@PathVariable("bid") Long brandId, @PathVariable("mid") Long modelId) {
		return mapper.modelToDto(carDataService.getModel(modelId).orElseThrow(() -> new LismoveException("Model not found", HttpStatus.NOT_FOUND)));
	}

	@GetMapping("{bid}/models/{mid}/generations")
	public List<CarGenerationDto> getGenerations(@PathVariable("bid") Long brandId, @PathVariable("mid") Long modelId) {
		CarModel model = carDataService.getModel(modelId).orElseThrow(() -> new LismoveException("Model not found", HttpStatus.NOT_FOUND));
		return mapper.generationToDto(carDataService.getGenerations(model));
	}

	@GetMapping("{bid}/models/{mid}/generations/{gid}")
	public CarGenerationDto getGeneration(@PathVariable("bid") Long brandId, @PathVariable("mid") Long modelId, @PathVariable("gid") Long generationId) {
		return mapper.generationToDto(carDataService.getGeneration(generationId).orElseThrow(() -> new LismoveException("Generation not found", HttpStatus.NOT_FOUND)));
	}

	@GetMapping("{bid}/models/{mid}/generations/{gid}/modifications")
	public List<CarModificationDto> getModifications(@PathVariable("bid") Long brandId, @PathVariable("mid") Long modelId, @PathVariable("gid") Long generationId) {
		CarGeneration generation = carDataService.getGeneration(generationId).orElseThrow(() -> new LismoveException("Generation not found", HttpStatus.NOT_FOUND));
		return mapper.modificationToDto(carDataService.getModifications(generation));
	}

}
