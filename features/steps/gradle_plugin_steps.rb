When("I build {string} using the {string} bugsnag config") do |module_config, bugsnag_config|
steps %Q{
  When I set environment variable "MODULE_CONFIG" to "#{module_config}"
  When I set environment variable "BUGSNAG_CONFIG" to "#{bugsnag_config}"
  And I run the script "features/scripts/build_project_module.sh" synchronously
  And I wait for 1 second
}
end
