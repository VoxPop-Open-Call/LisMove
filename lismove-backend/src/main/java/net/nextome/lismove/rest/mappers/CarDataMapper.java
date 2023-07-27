package net.nextome.lismove.rest.mappers;

import net.nextome.lismove.models.CarGeneration;
import net.nextome.lismove.models.CarModel;
import net.nextome.lismove.models.CarModification;
import net.nextome.lismove.rest.dto.CarGenerationDto;
import net.nextome.lismove.rest.dto.CarModelDto;
import net.nextome.lismove.rest.dto.CarModificationDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Service;

import java.util.List;

@Mapper(componentModel = "spring")
@Service
public abstract class CarDataMapper {

	@Mapping(target = "brand", source = "brand.id")
	public abstract CarModelDto modelToDto(CarModel model);

	@Mapping(target = "model", source = "model.id")
	public abstract CarGenerationDto generationToDto(CarGeneration generation);

	@Mapping(target = "generation", source = "generation.id")
	public abstract CarModificationDto modificationToDto(CarModification modification);

	public abstract List<CarModelDto> modelToDto(List<CarModel> model);

	public abstract List<CarGenerationDto> generationToDto(List<CarGeneration> generation);

	public abstract List<CarModificationDto> modificationToDto(List<CarModification> modification);
}
