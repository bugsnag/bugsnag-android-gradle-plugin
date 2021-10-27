BeforeAll do
  # Configure app environment
  # Set this explicitly
  $api_key = 'TEST_API_KEY'

  # Set which test fixture should be used
  ENV["APP_FIXTURE_DIR"] ||= "features/fixtures/app"
  ENV["NDK_FIXTURE_DIR"] ||= "features/fixtures/ndkapp"
  ENV["RN_FIXTURE_DIR"] ||= "features/fixtures/rn063/android"
  ENV["LIB_FIXTURE_DIR"] ||= "features/fixtures/mylib"
  ENV["RN_MONOREPO_FIXTURE_DIR"] ||= "features/fixtures/rn-monorepo/abc/android"
  ENV["UNITY_2018_FIXTURE_DIR"] ||= "features/fixtures/unity_2018/example"
  ENV["UNITY_2019_FIXTURE_DIR"] ||= "features/fixtures/unity_2019"

  # set defaults for versions
  ENV['AGP_VERSION'] ||= '4.1.0' # default to latest
  ENV['GRADLE_WRAPPER_VERSION'] ||= '6.5.1'

  Maze.config.enforce_bugsnag_integrity = false

  Maze::Runner.run_command('./features/scripts/clear_local_maven_repo.sh')
  Maze::Runner.run_command('./features/scripts/install_gradle_plugin.sh')
  Maze::Runner.run_command('./features/scripts/setup_gradle_wrapper.sh')
end

Before('@requires_agp4_0_or_higher') do |scenario|
  skip_this_scenario unless above_or_equal_to_target(400)
end

Before('@requires_agp4_1_or_higher') do |scenario|
  skip_this_scenario unless above_or_equal_to_target(410)
end

def above_or_equal_to_target(target)
  version = ENV['AGP_VERSION'].slice(0, 5)
  version = version.gsub('.', '')
  version.to_i >= target
end
