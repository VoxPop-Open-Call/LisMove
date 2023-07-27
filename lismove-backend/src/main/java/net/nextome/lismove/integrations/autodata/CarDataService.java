package net.nextome.lismove.integrations.autodata;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.nextome.lismove.integrations.autodata.models.AutodataResponse;
import net.nextome.lismove.models.CarBrand;
import net.nextome.lismove.models.CarGeneration;
import net.nextome.lismove.models.CarModel;
import net.nextome.lismove.models.CarModification;
import net.nextome.lismove.repositories.CarBrandRepository;
import net.nextome.lismove.repositories.CarGenerationRepository;
import net.nextome.lismove.repositories.CarModelRepository;
import net.nextome.lismove.repositories.CarModificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CarDataService {

	@Autowired
	private CarModelRepository modelRepository;
	@Autowired
	private CarBrandRepository brandRepository;
	@Autowired
	private CarGenerationRepository generationRepository;
	@Autowired
	private CarModificationRepository modificationRepository;

	public void importData(AutodataResponse response) {
		response.getBrands().getBrand().forEach(brand -> {
			CarBrand carBrand = brandRepository.save(new CarBrand(brand.getId(), brand.getName()));
			brand.getModels().getModel().forEach(model -> {
				CarModel carModel = modelRepository.save(new CarModel(model.getId(), model.getName(), carBrand));
				model.getGenerations().getGeneration().forEach(generation -> {
					CarGeneration carGeneration = generationRepository.save(new CarGeneration(generation.getId(), generation.getName(), generation.getModelYear(), carModel));
					generation.getModifications().getModification().forEach(modification -> {
						modificationRepository.save(new CarModification(modification.getId(), modification.getEngineDisplacement(), modification.getFuel(), modification.getFuelConsumptionUrban(), modification.getFuelConsumptionExtraurban(), modification.getCo2(), carGeneration));
					});
				});
			});
		});
	}

	public void generateData() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			importData(mapper.readValue(new ClassPathResource("examples/autodata.json").getURL(), AutodataResponse.class));
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public List<CarBrand> getBrands() {
		List<CarBrand> brands = new LinkedList<>();
		brandRepository.findAll().forEach(brands::add);
		return brands;
	}

	public Optional<CarBrand> getBrand(Long brandId) {
		return brandRepository.findById(brandId);
	}

	public List<CarModel> getModels(CarBrand brand) {
		return modelRepository.findByBrand(brand);
	}

	public Optional<CarModel> getModel(Long modelId) {
		return modelRepository.findById(modelId);
	}

	public List<CarGeneration> getGenerations(CarModel model) {
		return generationRepository.findByModel(model);
	}

	public Optional<CarGeneration> getGeneration(Long generationId) {
		return generationRepository.findById(generationId);
	}

	public List<CarModification> getModifications(CarGeneration generation) {
		return modificationRepository.findByGeneration(generation);
	}

	public Optional<CarModification> getModification(Long modificationId) {
		return modificationRepository.findById(modificationId);
	}

}
