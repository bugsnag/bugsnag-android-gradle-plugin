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

AfterConfiguration do |_config|
  Maze.config.enforce_bugsnag_integrity = false

  Maze::Runner.run_command('./features/scripts/clear_local_maven_repo.sh')
  Maze::Runner.run_command('./features/scripts/setup_gradle_wrapper.sh')
  Maze::Runner.run_command('./features/scripts/install_gradle_plugin.sh')
end

Before('@requires_agp4_0_or_higher') do |scenario|
  skip_this_scenario unless above_or_equal_to_target(400)
end

Before('@requires_agp4_1_or_higher') do |scenario|
  skip_this_scenario unless above_or_equal_to_target(410)
end

Before('@skip_agp4_0_or_higher') do |scenario|
  skip_this_scenario if above_or_equal_to_target(400)
end

Before('@skip_agp4_1_or_higher') do |scenario|
  skip_this_scenario if above_or_equal_to_target(410)
end

Before('@skip_gradle_7_or_higher') do |scenario|
  version = ENV['GRADLE_WRAPPER_VERSION'].slice(0, 1)
  skip_this_scenario if version.to_i >= 7
end

Before('@skip_agp3_4_0') do |scenario|
  skip_this_scenario if equals_target(340)
end

Before('@skip_rn60_fixture') do |scenario|
  skip_this_scenario if equals_rn_fixture('rn060')
end

def equals_target(target)
  version = ENV['AGP_VERSION'].slice(0, 5)
  version = version.gsub('.', '')
  version.to_i == target
end

def above_or_equal_to_target(target)
  version = ENV['AGP_VERSION'].slice(0, 5)
  version = version.gsub('.', '')
  version.to_i >= target
end

def equals_rn_fixture(target)
  fixture_name = ENV["RN_FIXTURE_DIR"].split('/')[-2]
  return fixture_name == target
end
