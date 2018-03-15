When("I build the {string} module") do |project_module|
steps %Q{
  When I set environment variable "PROJECT_MODULE" to "#{project_module}"
  And I run the script "features/scripts/build_project_module.sh" synchronously
  And I wait for 1 second
}
end
