=begin
 This file is part of Hopsworks
 Copyright (C) 2019, Logical Clocks AB. All rights reserved

 Hopsworks is free software: you can redistribute it and/or modify it under the terms of
 the GNU Affero General Public License as published by the Free Software Foundation,
 either version 3 of the License, or (at your option) any later version.

 Hopsworks is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License along with this program.
 If not, see <https://www.gnu.org/licenses/>.
=end
module JupyterHelper

  def start_jupyter(project, expected_status=200, shutdownLevel=6, baseDir=nil, gitConfig=nil, noLimit=false)
    settings = get_settings(project)

    if !baseDir.nil?
        settings[:baseDir] = baseDir
    end

    if !gitConfig.nil?
        settings[:gitBackend] = true
        settings[:gitConfig] = gitConfig
    end

    settings[:distributionStrategy] = ""
    settings[:shutdownLevel] = shutdownLevel
    settings[:noLimit] = noLimit
    staging_dir = settings[:privateDir]

    post "#{ENV['HOPSWORKS_API']}/project/#{project[:id]}/jupyter/start", JSON(settings)
    expect_status(expected_status)
    secret_dir = json_body[:secret]

    return secret_dir, staging_dir, settings
  end

  def get_settings(project)
      get "#{ENV['HOPSWORKS_API']}/project/#{project[:id]}/jupyter/settings"
      json_body
  end

  def is_git_available(project)
      settings = get_settings(project)
      settings[:gitAvailable]
  end

  def stop_jupyter(project)
    get "#{ENV['HOPSWORKS_API']}/project/#{project[:id]}/jupyter/stop"
    expect_status(200)
  end

  def create_notebook(jupyter_port)
    json_result = post "/hopsworks-api/jupyter/#{jupyter_port}/api/contents", {type: "notebook", path: ""}
    expect_status(201)
    parsed_json = JSON.parse(json_result)
    temp_name = parsed_json["name"]

    return temp_name
  end

  def update_notebook(jupyter_port, content, notebook_name)
    put "/hopsworks-api/jupyter/#{jupyter_port}/api/contents/#{notebook_name}", {content: content, format:"json", path:
      notebook_name, type:"notebook"}
    expect_status_details(200)
  end

  def read_notebook(file_path)
    file = File.read(file_path)
    notebook_json = JSON.parse(file)
    return notebook_json
  end

  def get_notebook_code(notebook)
    notebook_code = []
    #get the notebook code cells
    for cell in notebook["cells"] do
      if cell["cell_type"] == "code" then
        cell_source_code = cell["source"]
        if cell_source_code.length() > 0 then
          notebook_code.push(cell_source_code)
        end
      end
    end
    return notebook_code
  end

  def create_websocket_connection_to_jupyter_server(port, kernel_id, session_id)
    ws = WebSocket::Client::Simple.connect "wss://localhost:8181/hopsworks-api/jupyter/#{port}/api/kernels/#{kernel_id}/channels?session_id=#{session_id}"
    return ws
  end

  def create_notebook_session(jupyter_port, notebook_name, path=SecureRandom.uuid)
    json_result = post "/hopsworks-api/jupyter/#{jupyter_port}/api/sessions", {path:path, name:notebook_name,
                                                                       type:"notebook"}
    expect_status_details(201)
    notebook_session = JSON.parse(json_result)
    kernel_id = notebook_session["kernel"]["id"]
    session_id = notebook_session["id"]

    return session_id, kernel_id
  end

  def list_content(port, token)
    get "#{ENV['HOPSWORKS_BASE_API']}jupyter/#{port}/api/contents?token=#{token}"
    json_body
  end

  def get_git_config(git_backend, repo_url, shutdown_auto_push=false, base_branch="master", head_branch="master", api_key_name=nil)
    git_config = {
      "gitBackend": "#{git_backend}",
      "remoteGitURL": "#{repo_url}",
      "baseBranch": "#{base_branch}",
      "headBranch": "#{head_branch}",
      "apiKeyName": "#{api_key_name}",
      "startupAutoPull": true,
      "shutdownAutoPush": "#{shutdown_auto_push}"
    }
    git_config
  end

  def auth_token(token)
    begin
      bearer = ""
      Airborne.configure do |config|
        bearer = config.headers["Authorization"]
        config.headers["Authorization"] = "token #{token}"
      end
      yield
    ensure
      Airborne.configure do |config|
        config.headers["Authorization"] = bearer
      end
    end
  end

  def recentnotebooks_search(project, expected_count)
    wait_result = wait_for_me_time(10) do
      pp "#{ENV['HOPSWORKS_API']}/project/#{project[:id]}/jupyter/recent" if (defined?(@debugOpt)) && @debugOpt
      get "#{ENV['HOPSWORKS_API']}/project/#{project[:id]}/jupyter/recent"
      expect_status_details(200)
	  begin
		expect(json_body[:items]).not_to be_nil
        expect(json_body[:items].length).to eql(expected_count)
        { 'success' => true }
      rescue RSpec::Expectations::ExpectationNotMetError => e
        pp "rescued ex - retrying" if defined?(@debugOpt) && @debugOpt
        { 'success' => false, 'ex' => e }
      end
    end

    if expected_count > 0
      expect(json_body[:items][0][:jupyterConfiguration]).not_to be_nil
      expect(json_body[:items][0][:path]).not_to be_nil
    end
  end

  def attachConfiguration(project, hdfsUsername, kernelId)
    pp "#{ENV['HOPSWORKS_API']}/project/#{project[:id]}/jupyter/attachConfiguration/#{hdfsUsername}/#{kernelId}" if (defined?(@debugOpt)) && @debugOpt
    put "#{ENV['HOPSWORKS_API']}/project/#{project[:id]}/jupyter/attachConfiguration/#{hdfsUsername}/#{kernelId}"
    expect_status_details(200)
  end
end