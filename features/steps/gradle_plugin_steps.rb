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

Then(/^the request (\d+) is valid for the Android NDK Mapping API$/) do |request_index|
  parts = find_request(request_index)[:body]
  assert_not_nil(parts["soSymbolFile"], "'soSymbolFile' should not be nil")
  assert_not_nil(parts["apiKey"], "'apiKey' should not be nil")
  assert_not_nil(parts["sharedObjectName"], "'sharedObjectName' should not be nil")
  assert_not_nil(parts["appId"], "'appId' should not be nil")
  assert_not_nil(parts["arch"], "'arch' should not be nil")
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
