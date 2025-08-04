package lt.creditco.cupa.service.mapper;

import static lt.creditco.cupa.domain.ClientCardAsserts.*;
import static lt.creditco.cupa.domain.ClientCardTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClientCardMapperTest {

    private ClientCardMapper clientCardMapper;

    @BeforeEach
    void setUp() {
        clientCardMapper = new ClientCardMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getClientCardSample1();
        var actual = clientCardMapper.toEntity(clientCardMapper.toDto(expected));
        assertClientCardAllPropertiesEquals(expected, actual);
    }
}
