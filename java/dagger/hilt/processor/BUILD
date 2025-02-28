# Copyright (C) 2020 The Dagger Authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Description:
#   Hilt android processors.

load("//:build_defs.bzl", "POM_VERSION")
load("//tools:maven.bzl", "gen_maven_artifact")

package(default_visibility = ["//:src"])

# TODO(bcorso): merge this into :artifact-lib once we remove hilt-android-compiler artifact.
java_library(
    name = "artifact-lib-shared",
    exports = [
        "//java/dagger/hilt/android/processor/internal/androidentrypoint:processor_lib",
        "//java/dagger/hilt/android/processor/internal/bindvalue:bind_value_processor_lib",
        "//java/dagger/hilt/android/processor/internal/customtestapplication:processor_lib",
        "//java/dagger/hilt/android/processor/internal/viewmodel:processor_lib",
        "//java/dagger/hilt/android/processor/internal/viewmodel:validation_plugin_lib",
        "//java/dagger/hilt/processor/internal/aggregateddeps:processor_lib",
        "//java/dagger/hilt/processor/internal/aliasof:processor_lib",
        "//java/dagger/hilt/processor/internal/definecomponent:processor_lib",
        "//java/dagger/hilt/processor/internal/earlyentrypoint:processor_lib",
        "//java/dagger/hilt/processor/internal/generatesrootinput:processor_lib",
        "//java/dagger/hilt/processor/internal/originatingelement:processor_lib",
        "//java/dagger/hilt/processor/internal/root:component_tree_deps_processor_lib",
        "//java/dagger/hilt/processor/internal/root:root_processor_lib",
        "//java/dagger/hilt/processor/internal/uninstallmodules:processor_lib",
        "//java/dagger/internal/codegen:processor",
    ],
)

java_library(
    name = "artifact-lib",
    tags = ["maven_coordinates=com.google.dagger:hilt-compiler:" + POM_VERSION],
    visibility = ["//visibility:private"],
    exports = [
        ":artifact-lib-shared",
    ],
)

gen_maven_artifact(
    name = "artifact",
    artifact_coordinates = "com.google.dagger:hilt-compiler:" + POM_VERSION,
    artifact_name = "Hilt Processor",
    artifact_target = ":artifact-lib",
    artifact_target_libs = [
        "//java/dagger/hilt/android/processor/internal:android_classnames",
        "//java/dagger/hilt/android/processor/internal:utils",
        "//java/dagger/hilt/android/processor/internal/androidentrypoint:android_generators",
        "//java/dagger/hilt/android/processor/internal/androidentrypoint:metadata",
        "//java/dagger/hilt/android/processor/internal/androidentrypoint:processor_lib",
        "//java/dagger/hilt/android/processor/internal/bindvalue:bind_value_processor_lib",
        "//java/dagger/hilt/android/processor/internal/customtestapplication:processor_lib",
        "//java/dagger/hilt/android/processor/internal/viewmodel:validation_plugin_lib",
        "//java/dagger/hilt/android/processor/internal/viewmodel:processor_lib",
        "//java/dagger/hilt/processor/internal:aggregated_elements",
        "//java/dagger/hilt/processor/internal:base_processor",
        "//java/dagger/hilt/processor/internal:classnames",
        "//java/dagger/hilt/processor/internal:compiler_options",
        "//java/dagger/hilt/processor/internal:component_descriptor",
        "//java/dagger/hilt/processor/internal:component_names",
        "//java/dagger/hilt/processor/internal:components",
        "//java/dagger/hilt/processor/internal:element_descriptors",
        "//java/dagger/hilt/processor/internal:processor_errors",
        "//java/dagger/hilt/processor/internal:processors",
        "//java/dagger/hilt/processor/internal/aggregateddeps:component_dependencies",
        "//java/dagger/hilt/processor/internal/aggregateddeps:pkg_private_metadata",
        "//java/dagger/hilt/processor/internal/aggregateddeps:processor_lib",
        "//java/dagger/hilt/processor/internal/aliasof:alias_ofs",
        "//java/dagger/hilt/processor/internal/aliasof:processor_lib",
        "//java/dagger/hilt/processor/internal/definecomponent:define_components",
        "//java/dagger/hilt/processor/internal/definecomponent:processor_lib",
        "//java/dagger/hilt/processor/internal/earlyentrypoint:aggregated_early_entry_point_metadata",
        "//java/dagger/hilt/processor/internal/earlyentrypoint:processor_lib",
        "//java/dagger/hilt/processor/internal/generatesrootinput:generates_root_inputs",
        "//java/dagger/hilt/processor/internal/generatesrootinput:processor_lib",
        "//java/dagger/hilt/processor/internal/kotlin",
        "//java/dagger/hilt/processor/internal/originatingelement:processor_lib",
        "//java/dagger/hilt/processor/internal/root:component_tree_deps_processor_lib",
        "//java/dagger/hilt/processor/internal/root:root_processor_lib",
        "//java/dagger/hilt/processor/internal/root:root_metadata",
        "//java/dagger/hilt/processor/internal/root:root_type",
        "//java/dagger/hilt/processor/internal/root/ir:ir",
        "//java/dagger/hilt/processor/internal/uninstallmodules:processor_lib",
        "//java/dagger/hilt/processor/internal/uninstallmodules:aggregated_uninstall_modules_metadata",
    ],
    artifact_target_maven_deps = [
        "com.google.auto:auto-common",
        "com.google.code.findbugs:jsr305",
        "com.google.dagger:dagger-compiler",
        "com.google.dagger:dagger",
        "com.google.dagger:dagger-spi",
        "com.google.guava:failureaccess",
        "com.google.guava:guava",
        "com.squareup:javapoet",
        "javax.annotation:jsr250-api",
        "javax.inject:javax.inject",
        "net.ltgt.gradle.incap:incap",
        "org.jetbrains.kotlin:kotlin-stdlib",
        "org.jetbrains.kotlinx:kotlinx-metadata-jvm",
    ],
    javadoc_android_api_level = 30,
    javadoc_root_packages = [
        "dagger.hilt.processor",
        "dagger.hilt.android.processor",
    ],
    javadoc_srcs = [
        "//java/dagger/hilt:hilt_processing_filegroup",
    ],
    # The shaded deps are added using jarjar, but they won't be shaded until later
    # due to: https://github.com/google/dagger/issues/2765. For the shaded rules see
    # util/deploy-hilt.sh
    shaded_deps = ["//third_party/java/auto:common"],
)

filegroup(
    name = "srcs_filegroup",
    srcs = glob(["*"]),
)
