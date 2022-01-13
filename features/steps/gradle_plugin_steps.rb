require 'zlib'
require 'stringio'

When('I build {string} using the {string} bugsnag config') do |module_config, bugsnag_config|
  exit_code = setup_and_run_script(module_config, bugsnag_config, 'features/scripts/build_project_module.sh')
  assert(exit_code.zero?, "Expected script to complete with 0 exit code, got #{exit_code}")
end

When('I build the {string} variantOutput for {string} using the {string} bugsnag config') do |variant, module_config, bugsnag_config|
  exit_code = setup_and_run_script(module_config, bugsnag_config, 'features/scripts/upload_variant_mapping.sh', variant)
  assert(exit_code.zero?, "Expected script to complete with 0 exit code, got #{exit_code}")
end

When('I bundle {string} using the {string} bugsnag config') do |module_config, bugsnag_config|
  exit_code = setup_and_run_script(module_config, bugsnag_config, 'features/scripts/bundle_project_module.sh')
  assert(exit_code.zero?, "Expected script to complete with 0 exit code, got #{exit_code}")
end

When('I bundle the {string} variantOutput for {string} using the {string} bugsnag config') do |variant, module_config, bugsnag_config|
  exit_code = setup_and_run_script(module_config, bugsnag_config, 'features/scripts/bundle_one_flavor.sh', variant)
  assert(exit_code.zero?, "Expected script to complete with 0 exit code, got #{exit_code}")
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

When('I build the failing {string} using the {string} bugsnag config') do |module_config, bugsnag_config|
  exit_code = setup_and_run_script(module_config, bugsnag_config, 'features/scripts/build_project_module.sh')
  assert(exit_code != 0, "Expected script to fail with non-zero exit code, got #{exit_code}")
end

When('I build the failing {string} on AGP7 using the {string} bugsnag config') do |module_config, bugsnag_config|
  Maze::Runner.environment['AGP_VERSION'] = '7.0.0-alpha15'
  exit_code = setup_and_run_script(module_config, bugsnag_config, 'features/scripts/build_project_module.sh')
  assert(exit_code != 0, "Expected script to fail with non-zero exit code, got #{exit_code}")
end

def setup_and_run_script(module_config, bugsnag_config, script_path, variant = nil)
  Maze::Runner.environment['MODULE_CONFIG'] = module_config
  Maze::Runner.environment['BUGSNAG_CONFIG'] = bugsnag_config
  Maze::Runner.environment['VARIANT_OUTPUT_NAME'] = variant unless variant.nil?
  _, exit_code = Maze::Runner.run_script(script_path, blocking: true)
  exit_code
end

When("I build the failing {string} on AGP {string} using the {string} bugsnag config") do |module_config, agp_version, bugsnag_config|
steps %Q{
    When I set environment variable "AGP_VERSION" to "#{agp_version}"
    And I build the failing "#{module_config}" using the "#{bugsnag_config}" bugsnag config
}
end

Then('{int} requests are valid for the build API and match the following:') do |request_count, data_table|
  requests = get_requests_with_field('build', 'builderName')
  assert_equal(request_count, requests.length, 'Wrong number of build API requests')
  Maze::Assertions::RequestSetAssertions.assert_requests_match requests, data_table

  requests.each do |request|
    valid_build_api?(request[:body])
  end
end

Then('{int} requests are valid for the android mapping API and match the following:') do |request_count, data_table|
  requests = get_requests_with_field('build', 'proguard')
  assert_equal(request_count, requests.length, 'Wrong number of mapping API requests')
  Maze::Assertions::RequestSetAssertions.assert_requests_match requests, data_table

  requests.each do |request|
    valid_android_mapping_api?(request[:body])
  end
end

Then('{int} requests are valid for the android NDK mapping API and match the following:') do |request_count, data_table|
  requests = get_requests_with_field('build', 'soSymbolFile')
  assert_equal(request_count, requests.length, 'Wrong number of NDK mapping API requests')
  Maze::Assertions::RequestSetAssertions.assert_requests_match requests, data_table

  requests.each do |request|
    valid_android_ndk_mapping_api?(request[:body])
  end
end

Then('{int} requests are valid for the android unity NDK mapping API and match the following:') do |request_count, data_table|
  requests = get_requests_with_field('build', 'soSymbolTableFile')
  assert_equal(request_count, requests.length, 'Wrong number of android unity NDK mapping API requests')
  Maze::Assertions::RequestSetAssertions.assert_requests_match requests, data_table

  requests.each do |request|
    valid_android_unity_ndk_mapping_api?(request[:body])
  end
end

Then('{int} requests are valid for the JS source map API and match the following:') do |request_count, data_table|
  requests = get_requests_with_field('build', 'sourceMap')
  assert_equal(request_count, requests.length, 'Wrong number of JS source map API requests')
  Maze::Assertions::RequestSetAssertions.assert_requests_match requests, data_table

  requests.each do |request|
    valid_js_source_map_api?(request[:body])
  end
end

Then('{int} requests have an R8 mapping file with the following symbols:') do |request_count, data_table|
  requests = get_requests_with_field('build', 'proguard')
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

Then('the build request is valid for the Android Mapping API') do
  steps %(
    And the build payload field "apiKey" equals "#{$api_key}"
    And the build payload field "proguard" is not null
    And the build payload field "appId" is not null
    And the build payload field "versionCode" is not null
    And the build payload field "buildUUID" is not null
    And the build payload field "versionName" is not null
  )
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
  assert_equal($api_key, Maze::Helper.read_key_path(request_body, 'apiKey'))
  assert_not_nil(Maze::Helper.read_key_path(request_body, 'appVersion'))
  assert_not_nil(Maze::Helper.read_key_path(request_body, 'builderName'))
  assert_not_nil(Maze::Helper.read_key_path(request_body, 'sourceControl.revision'))
  assert_not_nil(Maze::Helper.read_key_path(request_body, 'metadata.os_name'))
  assert_not_nil(Maze::Helper.read_key_path(request_body, 'metadata.os_arch'))
  assert_not_nil(Maze::Helper.read_key_path(request_body, 'metadata.os_version'))
  assert_not_nil(Maze::Helper.read_key_path(request_body, 'metadata.java_version'))
  assert_not_nil(Maze::Helper.read_key_path(request_body, 'metadata.gradle_version'))
  assert_not_nil(Maze::Helper.read_key_path(request_body, 'metadata.git_version'))
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

def get_requests_with_field(request_type, name)
  all_requests = Maze::Server.list_for(request_type).clone
  all_requests.all.reject do |request|
    value = Maze::Helper.read_key_path(request[:body], name)
    value.nil?
  end
end
