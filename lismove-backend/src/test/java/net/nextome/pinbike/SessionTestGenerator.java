package net.nextome.lismove;

import net.nextome.lismove.models.enums.PartialType;
import net.nextome.lismove.rest.dto.PartialDto;
import net.nextome.lismove.rest.dto.SessionDto;
import net.nextome.lismove.rest.dto.SessionPointDto;
import net.nextome.lismove.services.utils.UtilitiesService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;

public class SessionTestGenerator {
	public enum TestCase {
		SESSION_WITH_SPEED_ERROR,
		SESSION_WITH_DISTANCE_ERROR,
		VALID,
		VALID_WITH_PAUSE,
		VALID_ON_FOOT,
		VALID_ACCELERATION_CHECK
	}

	public static SessionDto generate(TestCase testCase) {
		SessionDto dto = new SessionDto();
		dto.setType(0);
		dto.setDescription("descrizione");
		dto.setUid("1");
		dto.setInitiativePoints(0);
		dto.setNationalPoints(0);

		switch(testCase) {
			case SESSION_WITH_SPEED_ERROR:
				dto.setPartials(new ArrayList<PartialDto>() {{
//                   start point
//                   2021-03-18    08:02:57+01:00
					add(generatePartial(41.40328D, 2.17453D, 1616050977000L, PartialType.START));
//                   123.45m
//                   2021-03-18    08:05:57+01:00
					add(generatePartial(41.40438D, 2.17473D, 1616051157000L));
//                   destination point
//                   11241.81m
//                   2021-03-18    08:10:57+01:00
					add(generatePartial(41.50548D, 2.17483D, 1616051457000L, PartialType.END));
				}});
				dto.getPartials().forEach(p -> p.setDeltaRevs((double) UtilitiesService.randomNumber(1, 20)));
				break;
			case SESSION_WITH_DISTANCE_ERROR:
				dto.setPartials(new ArrayList<PartialDto>() {{
//                   start point
//                   2021-03-18    08:02:57+01:00
					add(generatePartial(41.40328D, 2.17453D, 1616050977000L, PartialType.START));
//                   123.45m
//                   2021-03-18    08:05:57+01:00
					add(generatePartial(41.40438D, 2.17473D, 1616051157000L, BigDecimal.valueOf(0.300)));
//                    destination point
//                    122.6m
//                    2021-03-18    08:10:57+01:00
					add(generatePartial(41.40548D, 2.17483D, 1616051457000L, BigDecimal.valueOf(0.350), PartialType.END));
				}});
				dto.getPartials().forEach(p -> p.setDeltaRevs((double) UtilitiesService.randomNumber(1, 20)));
				break;
			case VALID:
				dto.setPartials(new ArrayList<PartialDto>() {{
//                start point
					add(generatePartial(40.890976185409116, 16.95612915866637, 1616511600000L, PartialType.START));
//                518.61m
					add(generatePartial(40.893518533495026, 16.96130166704409, 1616511720000L));
//                410.1m
					add(generatePartial(40.89684997111785, 16.963394853910863, 1616511840000L));
//                391.43m
					add(generatePartial(40.89929725544826, 16.966742490773083, 1616511960000L));
//                613.67m
					add(generatePartial(40.90194400972216, 16.97314958923043, 1616512080000L));
//                772.48m
					add(generatePartial(40.902976372678836, 16.982238861835906, 1616512200000L));
//                976.21m
					add(generatePartial(40.9053036589024, 16.993439085927516, 1616512320000L));
//                732.64m
					add(generatePartial(40.908531417581564, 17.0010392058016, 1616512440000L));
//                375.39m
					add(generatePartial(40.91078430589201, 17.004366085022507, 1616512560000L));
//                669.19m
					add(generatePartial(40.91403109237626, 17.011071296856652, 1616512680000L));
					add(generatePartial(40.91635422084898, 17.01724363637767, 1616512800000L));
					add(generatePartial(40.91974242163014, 17.020489280506656, 1616512900000L));
					add(generatePartial(40.92022002755205, 17.026204167969002, 1616513020000L));
					add(generatePartial(40.91802188904271, 17.02633704833258, 1616513140000L));
					add(generatePartial(40.91979594617434, 17.0299548937041, 1616513260000L, PartialType.END));
				}});
				break;
			case VALID_WITH_PAUSE:
				dto.setPartials(new ArrayList<PartialDto>() {{
//                start point
					add(generatePartial(40.890976185409116, 16.95612915866637, 1616511600000L, PartialType.START));
//                518.61m
					add(generatePartial(40.893518533495026, 16.96130166704409, 1616511720000L));
//                410.1m
					add(generatePartial(40.89684997111785, 16.963394853910863, 1616511840000L));
//                391.43m
					add(generatePartial(40.89929725544826, 16.966742490773083, 1616511960000L));
//                613.67m
					add(generatePartial(40.90194400972216, 16.97314958923043, 1616512080000L, PartialType.PAUSE));
//                (1745.62m)
					add(generatePartial(40.9053036589024, 16.993439085927516, 1616512320000L, PartialType.RESUME));
//                732.64m
					add(generatePartial(40.908531417581564, 17.0010392058016, 1616512440000L));
//                375.39m
					add(generatePartial(40.91078430589201, 17.004366085022507, 1616512560000L));
//                669.19m
					add(generatePartial(40.91403109237626, 17.011071296856652, 1616512680000L));
					add(generatePartial(40.91635422084898, 17.01724363637767, 1616512800000L));
					add(generatePartial(40.91974242163014, 17.020489280506656, 1616512900000L));
					add(generatePartial(40.92022002755205, 17.026204167969002, 1616513020000L));
					add(generatePartial(40.91802188904271, 17.02633704833258, 1616513140000L));
					add(generatePartial(40.91979594617434, 17.0299548937041, 1616513260000L, PartialType.END));
				}});
				break;
			case VALID_ON_FOOT:
				dto.setPartials(new ArrayList<PartialDto>() {{
//                start point
					add(generatePartial(40.890976185409116, 16.95612915866637, 1616512000000L, PartialType.START));
//                518.61m
					add(generatePartial(40.893518533495026, 16.96130166704409, 1616512250000L));
//                410.1m
					add(generatePartial(40.89684997111785, 16.963394853910863, 1616512500000L));
//                391.43m
					add(generatePartial(40.89929725544826, 16.966742490773083, 1616512750000L));
//                613.67m
					add(generatePartial(40.90194400972216, 16.97314958923043, 1616513000000L, PartialType.END));
				}});
				break;
			case VALID_ACCELERATION_CHECK:
				dto.setPartials(new ArrayList<PartialDto>() {{
//                	start point
					add(generatePartial(40.890976185409116, 16.95612915866637, 1616511600000L, PartialType.START));
//                	42.44m
					add(generatePartial(40.89111723200462, 16.956598276006527, 1616511603000L));
//					53.55m
					add(generatePartial(40.89133795932569, 16.957164470441544, 1616511606000L));
//					51.14m
					add(generatePartial(40.89113334155266, 16.956619594995267, 1616511609000L));
//					152.76m
					add(generatePartial(40.9074604641632, 16.96096348305676, 1616511612000L));
//					133.83m
					add(generatePartial(40.891481904990336, 16.95828163171756, 1616511615000L));
//					61.74m
					add(generatePartial(40.891533717831535, 16.95901286787844, 1616511618000L));
//					60.87m
					add(generatePartial(40.891554610339284, 16.959736481443347, 1616511621000L));
//					45.12m
					add(generatePartial(40.89157390564134, 16.96027265120299, 1616511624000L, PartialType.END));
				}});
				break;
		}
		dto.setSessionPoints(new LinkedList<SessionPointDto>() {{
			add(new SessionPointDto(null, 12.0, 12.0, 1.0));
		}});
		dto.getPartials().get(0).setSensorDistance(BigDecimal.ZERO);
		dto.setStartTime(dto.getPartials().get(0).getTimestamp());
		dto.setEndTime(dto.getPartials().get(dto.getPartials().size() - 1).getTimestamp());
		dto.setNationalPoints(32);
		dto.setGyroDistance(BigDecimal.valueOf(7.8));
		return dto;
	}

	public static PartialDto generatePartial(Double lat, Double lng, Long time, BigDecimal sensorDistance, PartialType type) {
		PartialDto dto = generatePartial(lat, lng, time);
		dto.setSensorDistance(sensorDistance);
		dto.setType(type.ordinal());
		return dto;
	}

	public static PartialDto generatePartial(Double lat, Double lng, Long time, BigDecimal sensorDistance) {
		PartialDto dto = generatePartial(lat, lng, time);
		dto.setSensorDistance(sensorDistance);
		return dto;
	}

	public static PartialDto generatePartial(Double lat, Double lng, Long time, PartialType type) {
		PartialDto dto = new PartialDto();
		dto.setType(type.ordinal());
		dto.setLatitude(lat);
		dto.setLongitude(lng);
		dto.setTimestamp(time);
		return dto;
	}

	public static PartialDto generatePartial(Double lat, Double lng, Long time) {
		PartialDto dto = new PartialDto();
		dto.setType(3);
		dto.setLatitude(lat);
		dto.setLongitude(lng);
		dto.setTimestamp(time);
		return dto;
	}

}
