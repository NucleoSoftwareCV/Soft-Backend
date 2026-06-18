package com.hean.consigueventas.oonabe.mapper;

import com.hean.consigueventas.oonabe.category.dto.request.CategoryUpsertRequest;
import com.hean.consigueventas.oonabe.category.dto.response.CategoryResponse;
import com.hean.consigueventas.oonabe.category.entity.Category;
import com.hean.consigueventas.oonabe.category.mapper.CategoryMapper;
import com.hean.consigueventas.oonabe.catalog.dto.response.CityResponse;
import com.hean.consigueventas.oonabe.catalog.entity.City;
import com.hean.consigueventas.oonabe.catalog.mapper.CityMapper;
import com.hean.consigueventas.oonabe.common.enums.UserStatus;
import com.hean.consigueventas.oonabe.location.dto.response.LocationResponse;
import com.hean.consigueventas.oonabe.location.entity.Location;
import com.hean.consigueventas.oonabe.location.mapper.LocationMapper;
import com.hean.consigueventas.oonabe.user.dto.response.UserResponse;
import com.hean.consigueventas.oonabe.user.entity.Role;
import com.hean.consigueventas.oonabe.user.entity.User;
import com.hean.consigueventas.oonabe.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MapStructMapperContractTest {

    private final CategoryMapper categoryMapper = Mappers.getMapper(CategoryMapper.class);
    private final CityMapper cityMapper = Mappers.getMapper(CityMapper.class);
    private final LocationMapper locationMapper = Mappers.getMapper(LocationMapper.class);
    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Test
    void mapsCategoryEntityAndCreateDto() {
        Category category = new Category();
        category.setId(5L);
        category.setName("Yoga");
        category.setDescription("Bienestar corporal");
        category.setActive(true);

        CategoryResponse dto = categoryMapper.toDto(category);
        Category entity = categoryMapper.toEntity(new CategoryUpsertRequest("Pilates", "Control corporal"));

        assertThat(dto).isEqualTo(new CategoryResponse(5L, "Yoga", "Bienestar corporal", true));
        assertThat(entity.getName()).isEqualTo("Pilates");
        assertThat(entity.getDescription()).isEqualTo("Control corporal");
    }

    @Test
    void mapsLocationEntity() {
        Location location = new Location();
        location.setId(9L);
        location.setName("Sede Centro");
        location.setAddress("Av. Principal 123");
        location.setReference("Segundo piso");
        location.setIsActive(true);

        LocationResponse dto = locationMapper.toDto(location);

        assertThat(dto).isEqualTo(new LocationResponse(9L, "Sede Centro", "Av. Principal 123", "Segundo piso", true));
    }

    @Test
    void mapsCityEntity() {
        City city = new City();
        city.setId(4L);
        city.setName("Lima");
        city.setProvince("Lima");
        city.setCountryCode("PE");
        city.setIsActive(true);

        CityResponse dto = cityMapper.toDto(city);

        assertThat(dto).isEqualTo(new CityResponse(4L, "Lima", "Lima", "PE", true));
    }

    @Test
    void mapsUserEntityAndNormalizesRoles() {
        User user = new User();
        user.setId(3L);
        user.setUsername("ana");
        user.setEmail("ana@example.com");
        user.setStatus(UserStatus.ACTIVO);
        user.setCreatedAt(LocalDateTime.of(2026, 6, 16, 10, 0));
        user.setRoles(Set.of(
                Role.builder().name("ROLE_ADMIN").build(),
                Role.builder().name("ROLE_USER").build()
        ));

        UserResponse dto = userMapper.toDto(user);
        List<UserResponse> list = userMapper.toDtoList(List.of(user));

        assertThat(dto.id()).isEqualTo(3L);
        assertThat(dto.active()).isTrue();
        assertThat(dto.roles()).containsExactlyInAnyOrder("ADMIN", "USER");
        assertThat(list).containsExactly(dto);
    }
}
