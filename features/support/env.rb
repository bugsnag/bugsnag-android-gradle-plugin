# Configure app environment

# Set which test fixture should be used
ENV["APP_FIXTURE_DIR"] ||= "features/fixtures/app"
ENV["NDK_FIXTURE_DIR"] ||= "features/fixtures/ndkapp"
ENV["RN_FIXTURE_DIR"] ||= "features/fixtures/rnapp/android"

run_required_commands([
  ["./features/scripts/clear_local_maven_repo.sh"],
  ["./features/scripts/install_gradle_plugin.sh"],
])
