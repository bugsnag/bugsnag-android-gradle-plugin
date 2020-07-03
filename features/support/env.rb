# Configure app environment

# Set which test fixture should be used
if ENV["APP_FIXTURE_DIR"] == nil
  ENV["APP_FIXTURE_DIR"] = "features/fixtures/app"
end

if ENV["NDK_FIXTURE_DIR"] == nil
  ENV["NDK_FIXTURE_DIR"] = "features/fixtures/ndkapp"
end

if ENV["RN_FIXTURE_DIR"] == nil
  ENV["RN_FIXTURE_DIR"] = "features/fixtures/rnapp/android"
end

run_required_commands([
  ["./features/scripts/clear_local_maven_repo.sh"],
  ["./features/scripts/install_gradle_plugin.sh"],
])
