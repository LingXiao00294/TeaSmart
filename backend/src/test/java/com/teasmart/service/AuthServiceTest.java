package com.teasmart.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.teasmart.common.BusinessException;
import com.teasmart.common.JwtUtil;
import com.teasmart.dto.ChangePasswordRequest;
import com.teasmart.dto.UpdateProfileRequest;
import com.teasmart.entity.User;
import com.teasmart.mapper.UserMapper;
import com.teasmart.vo.UserVO;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @BeforeAll
    static void initMybatisPlusCache() {
        // 初始化 MyBatis-Plus 实体元数据，使 LambdaUpdateWrapper 在单元测试中可用
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""), User.class);
    }

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setUsername("testuser");
        sampleUser.setPassword("$2a$encodedOld");
        sampleUser.setPhone("13800000000");
        sampleUser.setRole("USER");
    }

    private static ChangePasswordRequest changeReq(String oldPw, String newPw) {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setOldPassword(oldPw);
        req.setNewPassword(newPw);
        return req;
    }

    private static UpdateProfileRequest profileReq(String phone) {
        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setPhone(phone);
        return req;
    }

    // ---- changePassword ----

    @Nested
    @DisplayName("changePassword")
    class ChangePasswordTests {

        @Test
        @DisplayName("成功修改密码——仅更新 password 列")
        void success() {
            when(userMapper.selectById(1L)).thenReturn(sampleUser);
            when(passwordEncoder.matches("oldPass", "$2a$encodedOld")).thenReturn(true);
            when(passwordEncoder.encode("newPass123")).thenReturn("$2a$encodedNew");

            authService.changePassword(1L, changeReq("oldPass", "newPass123"));

            // 验证使用 LambdaUpdateWrapper 而非 updateById，只更新 password 列
            @SuppressWarnings("unchecked")
            ArgumentCaptor<LambdaUpdateWrapper<User>> captor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
            verify(userMapper).update(eq(null), captor.capture());
            verify(userMapper, never()).updateById(any(User.class));
        }

        @Test
        @DisplayName("新密码与原密码相同应拒绝")
        void samePassword() {
            assertThatThrownBy(() ->
                    authService.changePassword(1L, changeReq("same", "same")))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("新密码不能与原密码相同");

            verify(userMapper, never()).selectById(any());
        }

        @Test
        @DisplayName("用户不存在应抛出 notFound")
        void userNotFound() {
            when(userMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() ->
                    authService.changePassword(999L, changeReq("old", "newPass123")))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("用户不存在");
        }

        @Test
        @DisplayName("原密码不正确应拒绝")
        void wrongOldPassword() {
            when(userMapper.selectById(1L)).thenReturn(sampleUser);
            when(passwordEncoder.matches("wrong", "$2a$encodedOld")).thenReturn(false);

            assertThatThrownBy(() ->
                    authService.changePassword(1L, changeReq("wrong", "newPass123")))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("原密码不正确");
        }
    }

    // ---- updateProfile ----

    @Nested
    @DisplayName("updateProfile")
    class UpdateProfileTests {

        @Test
        @DisplayName("成功更新手机号")
        void success() {
            when(userMapper.selectById(1L)).thenReturn(sampleUser);

            UserVO vo = authService.updateProfile(1L, profileReq("13900000000"));

            assertThat(vo.getPhone()).isEqualTo("13900000000");
            verify(userMapper).update(eq(null), any(LambdaUpdateWrapper.class));
        }

        @Test
        @DisplayName("清空手机号（null）")
        void clearPhone() {
            when(userMapper.selectById(1L)).thenReturn(sampleUser);

            UserVO vo = authService.updateProfile(1L, profileReq(null));

            assertThat(vo.getPhone()).isNull();
            verify(userMapper).update(eq(null), any(LambdaUpdateWrapper.class));
        }

        @Test
        @DisplayName("用户不存在应抛出 notFound")
        void userNotFound() {
            when(userMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() ->
                    authService.updateProfile(999L, profileReq("13900000000")))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("用户不存在");
        }
    }
}
