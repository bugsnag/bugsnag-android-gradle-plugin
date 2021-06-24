require 'zlib'
require 'stringio'

When("I build {string} using the {string} bugsnag config") do |module_config, bugsnag_config|
steps %Q{
  When I set environment variable "MODULE_CONFIG" to "#{module_config}"
  When I set environment variable "BUGSNAG_CONFIG" to "#{bugsnag_config}"
  And I run the script "features/scripts/build_project_module.sh" synchronously
}
end

When("I build the {string} variantOutput for {string} using the {string} bugsnag config") do |variant, module_config, bugsnag_config|
steps %Q{
  When I set environment variable "VARIANT_OUTPUT_NAME" to "#{variant}"
  When I set environment variable "MODULE_CONFIG" to "#{module_config}"
  When I set environment variable "BUGSNAG_CONFIG" to "#{bugsnag_config}"
  And I run the script "features/scripts/upload_variant_mapping.sh" synchronously
}
end

When("I bundle {string} using the {string} bugsnag config") do |module_config, bugsnag_config|
steps %Q{
  When I set environment variable "MODULE_CONFIG" to "#{module_config}"
  When I set environment variable "BUGSNAG_CONFIG" to "#{bugsnag_config}"
  And I run the script "features/scripts/bundle_project_module.sh" synchronously
}
end

When("I bundle the {string} variantOutput for {string} using the {string} bugsnag config") do |variant, module_config, bugsnag_config|
steps %Q{
  When I set environment variable "VARIANT_OUTPUT_NAME" to "#{variant}"
  When I set environment variable "MODULE_CONFIG" to "#{module_config}"
  When I set environment variable "BUGSNAG_CONFIG" to "#{bugsnag_config}"
  And I run the script "features/scripts/bundle_one_flavor.sh" synchronously
}
end

When("I build the React Native app") do
steps %Q{
  And I run the script "features/scripts/build_react_native_app.sh" synchronously
}
end

When("I build the NDK app") do
steps %Q{
  And I run the script "features/scripts/build_ndk_app.sh" synchronously
}
end

When("I set the fixture JVM arguments to {string}") do |jvm_args|
steps %Q{
  When I set environment variable "CUSTOM_JVM_ARGS" to "#{jvm_args}"
}
end

When("I build the failing {string} on AGP7 using the {string} bugsnag config") do |module_config, bugsnag_config|
steps %Q{
    When I set environment variable "AGP_VERSION" to "7.0.0-alpha15"
    And I build the failing "#{module_config}" using the "#{bugsnag_config}" bugsnag config
}
end

When("I build the failing {string} using the {string} bugsnag config") do |module_config, bugsnag_config|
  Runner.environment["MODULE_CONFIG"] = module_config
  Runner.environment["BUGSNAG_CONFIG"] = bugsnag_config
  _, exit_code = Runner.run_script("features/scripts/bundle_project_module.sh", blocking: true)
  assert(exit_code != 0, "Expected script to fail with non-zero exit code, got #{exit_code}")
end

Then(/^the exit code equals (\d+)$/) do |exit_code|
  assert_equal(exit_code, $?.exitstatus.to_i)
end

Then('{int} requests are valid for the build API and match the following:') do |request_count, data_table|
  requests = get_requests_with_field('builderName')
  assert_equal(request_count, requests.length, 'Wrong number of build API requests')
  RequestSetAssertions.assert_requests_match requests, data_table

  requests.each do |request|
    valid_build_api?(request[:body])
  end
end

Then('{int} requests are valid for the android mapping API and match the following:') do |request_count, data_table|
  requests = get_requests_with_field('proguard')
  assert_equal(request_count, requests.length, 'Wrong number of mapping API requests')
  RequestSetAssertions.assert_requests_match requests, data_table

  requests.each do |request|
    valid_android_mapping_api?(request[:body])
  end
end

Then('{int} requests are valid for the android NDK mapping API and match the following:') do |request_count, data_table|
  requests = get_requests_with_field('soSymbolFile')
  assert_equal(request_count, requests.length, 'Wrong number of NDK mapping API requests')
  RequestSetAssertions.assert_requests_match requests, data_table

  requests.each do |request|
    valid_android_ndk_mapping_api?(request[:body])
  end
end

Then('{int} requests are valid for the android unity NDK mapping API and match the following:') do |request_count, data_table|
  requests = get_requests_with_field('soSymbolTableFile')
  assert_equal(request_count, requests.length, 'Wrong number of android unity NDK mapping API requests')
  RequestSetAssertions.assert_requests_match requests, data_table

  requests.each do |request|
    valid_android_unity_ndk_mapping_api?(request[:body])
  end
end


Then('{int} requests are valid for the JS source map API and match the following:') do |request_count, data_table|
  requests = get_requests_with_field('sourceMap')
  assert_equal(request_count, requests.length, 'Wrong number of JS source map API requests')
  RequestSetAssertions.assert_requests_match requests, data_table

  requests.each do |request|
    valid_js_source_map_api?(request[:body])
  end
end

Then('{int} requests have an R8 mapping file with the following symbols:') do |request_count, data_table|
  requests = get_requests_with_field('proguard')
  assert_equal(request_count, requests.length, 'Wrong number of mapping API requests')

  # inflate gzipped proguard mapping file & verify contents
  requests.each do |request|
    valid_android_mapping_api?(request[:body])
    gzipped_part = request[:body]['proguard']
    archive = Zlib::GzipReader.new(StringIO.new(gzipped_part))
    mapping_file_lines = archive.read.split("\n")
    valid_r8_mapping_contents?(mapping_file_lines, data_table.rows)
  end
end

def valid_r8_mapping_contents?(mapping_file_lines, expected_entries)
  # validates that the mapping file key is present for each symbol,
  # obfuscated values are not validated as they vary depending on AGP's implementation
  expected_entries.each do |row|
    expected_entry = row[0] + " ->"
    has_mapping_entry = mapping_file_lines.one? { |line|
      line.include? expected_entry
    }
    assert_true(has_mapping_entry, "No entry in mapping file for '#{row[0]}'.")
  end
end

def valid_build_api?(request_body)
  assert_equal($api_key, read_key_path(request_body, 'apiKey'))
  assert_not_nil(read_key_path(request_body, 'appVersion'))
  assert_not_nil(read_key_path(request_body, 'builderName'))
  assert_not_nil(read_key_path(request_body, 'sourceControl.revision'))
  assert_not_nil(read_key_path(request_body, 'metadata.os_name'))
  assert_not_nil(read_key_path(request_body, 'metadata.os_arch'))
  assert_not_nil(read_key_path(request_body, 'metadata.os_version'))
  assert_not_nil(read_key_path(request_body, 'metadata.java_version'))
  assert_not_nil(read_key_path(request_body, 'metadata.gradle_version'))
  assert_not_nil(read_key_path(request_body, 'metadata.git_version'))
end

def valid_android_mapping_api?(request_body)
  valid_mapping_api?(request_body)
  assert_not_nil(request_body['proguard'])
end

def valid_android_ndk_mapping_api?(request_body)
  valid_mapping_api?(request_body)
  assert_not_nil(request_body['soSymbolFile'])
end

def valid_android_unity_ndk_mapping_api?(request_body)
  valid_mapping_api?(request_body)
  assert_not_nil(request_body['soSymbolTableFile'])
end

def valid_mapping_api?(request_body)
  assert_equal($api_key, request_body['apiKey'])
  assert_not_nil(request_body['appId'])
  assert_not_nil(request_body['versionCode'])
  assert_not_nil(request_body['buildUUID'])
  assert_not_nil(request_body['versionName'])
end

def valid_js_source_map_api?(request_body)
  assert_equal($api_key, request_body['apiKey'])
  assert_equal('android', request_body['platform'])
  assert_not_nil(request_body['sourceMap'])
  assert_not_nil(request_body['bundle'])
end
