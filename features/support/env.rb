# Configure app environment

# Set which test fixture should be used
ENV["APP_FIXTURE_DIR"] ||= "features/fixtures/app"
ENV["NDK_FIXTURE_DIR"] ||= "features/fixtures/ndkapp"
ENV["RN_FIXTURE_DIR"] ||= "features/fixtures/rn065/android"
ENV["LIB_FIXTURE_DIR"] ||= "features/fixtures/mylib"
ENV["RN_MONOREPO_FIXTURE_DIR"] ||= "features/fixtures/rn-monorepo/abc/android"
ENV["UNITY_2018_FIXTURE_DIR"] ||= "features/fixtures/unity_2018/example"
ENV["UNITY_2019_FIXTURE_DIR"] ||= "features/fixtures/unity_2019"

# set defaults for versions
ENV["AGP_VERSION"] ||= "7.0.0" # default to latest
ENV["GRADLE_WRAPPER_VERSION"] ||= "7.0.2"

`./features/scripts/clear_local_maven_repo.sh`
`./features/scripts/setup_rn.sh`
`./features/scripts/install_gradle_plugin.sh`

Before('@skip_gradle_7_or_higher') do |scenario|
  version = ENV["GRADLE_WRAPPER_VERSION"].slice(0, 1)
  skip_this_scenario if version.to_i >= 7
end

def get_requests_with_field(name)
  Server.stored_requests.reject do |request|
    value = read_key_path(request[:body], name)
    value.nil?
  end
end

$api_key = "TEST_API_KEY"
