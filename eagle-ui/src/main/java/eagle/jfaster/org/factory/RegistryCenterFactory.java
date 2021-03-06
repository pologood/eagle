/*
 * Copyright 2017 eagle.jfaster.org.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package eagle.jfaster.org.factory;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import eagle.jfaster.org.CoordinatorRegistryCenter;
import eagle.jfaster.org.ZookeeperRegistryCenter;
import eagle.jfaster.org.config.ConfigEnum;
import eagle.jfaster.org.config.common.MergeConfig;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 注册中心工厂.
 *
 * @author fangyanpeng
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RegistryCenterFactory {

    private static final ConcurrentHashMap<HashCode, CoordinatorRegistryCenter> REG_CENTER_REGISTRY = new ConcurrentHashMap<>();

    /**
     * 创建注册中心.
     *
     * @param connectString 注册中心连接字符串
     * @param namespace 注册中心命名空间
     * @param digest 注册中心凭证
     * @return 注册中心对象
     */
    public static CoordinatorRegistryCenter createCoordinatorRegistryCenter(final String connectString, final String namespace, final Optional<String> digest) {
        Hasher hasher = Hashing.md5().newHasher().putString(connectString, Charsets.UTF_8).putString(namespace, Charsets.UTF_8);
        if (digest.isPresent()) {
            hasher.putString(digest.get(), Charsets.UTF_8);
        }
        HashCode hashCode = hasher.hash();
        CoordinatorRegistryCenter result = REG_CENTER_REGISTRY.get(hashCode);
        if (null != result) {
            return result;
        }
        MergeConfig zkConfig = new MergeConfig();
        zkConfig.addExt(ConfigEnum.address.getName(), connectString);
        zkConfig.addExt(ConfigEnum.namespace.getName(), namespace);
        if (digest.isPresent()) {
            zkConfig.addExt(ConfigEnum.digest.getName(), digest.get());
        }
        result = new ZookeeperRegistryCenter(zkConfig);
        result.init();
        REG_CENTER_REGISTRY.put(hashCode, result);
        return result;
    }
}
