package net.nextome.lismove.rest.mappers;

import net.nextome.lismove.models.Sensor;
import net.nextome.lismove.rest.dto.SensorDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Service;

import java.util.List;

@Mapper(componentModel = "spring")
@Service
public abstract class SensorMapper extends UtilMapper{

    public abstract Sensor dtoToSensor(SensorDto dto);

    public abstract SensorDto sensorToDto(Sensor sensor);

    public abstract List<SensorDto> sensorToDto(List<Sensor> sensor);
}
