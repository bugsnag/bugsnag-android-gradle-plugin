# Configure app environment
# Set this explicitly
$api_key = 'TEST_API_KEY'

# Set which test fixture should be used
ENV['APP_FIXTURE_DIR'] ||= 'features/fixtures/app'
ENV['NDK_FIXTURE_DIR'] ||= 'features/fixtures/ndkapp'
ENV['RN_FIXTURE_DIR'] ||= 'features/fixtures/rn063/android'
ENV['LIB_FIXTURE_DIR'] ||= "features/fixtures/mylib"
ENV['RN_MONOREPO_FIXTURE_DIR'] ||= 'features/fixtures/rn-monorepo/abc/android'
ENV['UNITY_2018_FIXTURE_DIR'] ||= 'features/fixtures/unity_2018/example'
ENV['UNITY_2019_FIXTURE_DIR'] ||= 'features/fixtures/unity_2019'

# set defaults for versions
ENV['AGP_VERSION'] ||= '7.0.0' # default to latest
ENV['GRADLE_WRAPPER_VERSION'] ||= '7.0.2'

`./features/scripts/clear_local_maven_repo.sh`
`./features/scripts/setup_rn.sh`
`./features/scripts/install_gradle_plugin.sh`

AfterConfiguration do |_config|
  Maze.config.enforce_bugsnag_integrity = false
end

Before('@requires_agp4_1_or_higher') do |scenario|
  skip_this_scenario unless above_or_equal_to_target(410)
end

def above_or_equal_to_target(target)
  version = ENV['AGP_VERSION'].slice(0, 5)
  version = version.gsub('.', '')
  version.to_i >= target
end
