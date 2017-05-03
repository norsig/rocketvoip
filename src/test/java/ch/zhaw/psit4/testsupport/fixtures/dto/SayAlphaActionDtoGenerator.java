package ch.zhaw.psit4.testsupport.fixtures.dto;

import ch.zhaw.psit4.dto.actions.SayAlphaActionDto;
import ch.zhaw.psit4.testsupport.fixtures.general.SayAlphaActionData;

/**
 * @author Jona Braun
 */
public class SayAlphaActionDtoGenerator {
    public static final long NON_EXISTING_ID = 100;

    private SayAlphaActionDtoGenerator() {
        //intentionally empty
    }

    public static SayAlphaActionDto createTestDialActionDto(long number) {
        SayAlphaActionDto sayAlphaActionDto = new SayAlphaActionDto();
        sayAlphaActionDto.setSleepTime(SayAlphaActionData.getSleepTime((int) number));
        sayAlphaActionDto.setVoiceMessage(SayAlphaActionData.getVoiceMessage((int) number));
        return sayAlphaActionDto;
    }
}