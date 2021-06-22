# Configure app environment
# Set this explicitly
$api_key = 'TEST_API_KEY'

# Set which test fixture should be used
ENV['APP_FIXTURE_DIR'] ||= 'features/fixtures/app'
ENV['NDK_FIXTURE_DIR'] ||= 'features/fixtures/ndkapp'
ENV['RN_FIXTURE_DIR'] ||= 'features/fixtures/rn063/android'
ENV['RN_MONOREPO_FIXTURE_DIR'] ||= 'features/fixtures/rn-monorepo/abc/android'
ENV['UNITY_2018_FIXTURE_DIR'] ||= 'features/fixtures/unity_2018/example'
ENV['UNITY_2019_FIXTURE_DIR'] ||= 'features/fixtures/unity_2019'

# set defaults for versions
ENV['AGP_VERSION'] ||= '4.1.0' # default to latest
ENV['GRADLE_WRAPPER_VERSION'] ||= '6.5.1'

`./features/scripts/clear_local_maven_repo.sh`
`./features/scripts/setup_gradle_wrapper.sh`
`./features/scripts/install_gradle_plugin.sh`

Before('@requires_agp4_0_or_higher') do |scenario|
  skip_this_scenario unless is_above_or_equal_to_target(400)
end

Before('@requires_agp4_1_or_higher') do |scenario|
  skip_this_scenario unless is_above_or_equal_to_target(410)
end

Before('@skip_agp4_0_or_higher') do |scenario|
  skip_this_scenario if is_above_or_equal_to_target(400)
end

Before('@skip_agp4_1_or_higher') do |scenario|
  skip_this_scenario if is_above_or_equal_to_target(410)
end

Before('@skip_gradle_7_or_higher') do |scenario|
  version = ENV['GRADLE_WRAPPER_VERSION'].slice(0, 1)
  skip_this_scenario if version.to_i >= 7
end

Before('@skip_agp3_4_0') do |scenario|
  skip_this_scenario if equals_target(340)
end
