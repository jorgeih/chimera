(function(){
	var userIn = angular.module('userInteraction', []);
	
	// Create a directive for Scenarios Menu Entry
	userIn.directive('scenarioMenuEntry', function(){
		return {
			restrict: 'A',
			templateUrl: 'asset/templates/scenarioMenuEntry.html',
		};
	});

	// Create a Controller for the Scenario Information
	userIn.controller('ScenarioController', ['$routeParams', '$location', '$http', '$scope',
		function($routeParams, $location, $http){
			var controller = this;
			
			// initialize an empty list of scenario Ids
            this.currentScenario = {};
			this.scenarios = {};
			
			$http.get(JEngine_Server_URL + "/" + JCore_REST_Interface + "/scenario/").
				success(function(data){
					controller.scenarios = data;
					});

            this.getInstancesOfScenario = function(id) {
                $http.get(JEngine_Server_URL + "/" + JCore_REST_Interface + "/scenario/" + id + "/instance/").
                    success(function(data) {
                        controller.currentScenario['instances'] = data;
                    }).
                    error(function() {
                        console.log('request failed');
                    });
            };

            this.getTerminationConditionOfScenario = function(id) {
                $http.get(JEngine_Server_URL + "/" + JCore_REST_Interface + "/scenario/" + id + "/terminationcondition/").
                    success(function(data) {
                        controller.currentScenario['terminationcondition'] = data;
                    }).
                    error(function() {
                        console.log('request failed');
                    });
            };

            //if we are within the layer scenario
            if ($routeParams.id != null) {
                //setting current id of scenario based on the URI
                controller.currentScenario['id'] = $routeParams.id;
                //calling details for this scenario
                $http.get(JEngine_Server_URL + "/" + JCore_REST_Interface + "/scenario/" + controller.currentScenario['id'] + "/").
                    success(function(data) {
                        controller.currentScenario['details'] = data;
                    }).
                    error(function() {
                        console.log('request failed');
                    });
                controller.getInstancesOfScenario(controller.currentScenario['id']);
                controller.getTerminationConditionOfScenario(controller.currentScenario['id']);
            }

			this.getScenarioDetails = function(id){
					$http.get(JEngine_Server_URL + "/" + JCore_REST_Interface + "/scenario/" + id + "/").
						success(function(data) {
							controller.currentScenario['details'] = data;
						}).
                        error(function() {
                            console.log('request failed');
                        });
			};
			
			this.getImageForScenario = function(id){
				this.scenarios["" + id]['imageUrl'] =
					JEngine_Server_URL + "/" + JComparser_REST_Interface + "/scenarios/" + id + "/image/";
			};
			
			this.goToDetailsFrom = function(id){
				$location.path('scenario/' + id);
			};
			
			this.getCurrentScenario = function(){
				if ($routeParams.id != null) {
                    controller.getScenarioDetails($routeParams.id);
				}
			};

            this.deleteScenario = function(id){
                $http.delete(JEngine_Server_URL + "/" + JConfig_REST_Interface +
                "/scenario/" + id + "/?").
                    success(function(data) {
                        console.log("deleting scenario was successful..");
                    });
                //load new scenario list
                $http.get(JEngine_Server_URL + "/" + JCore_REST_Interface + "/scenario/").
                    success(function(data){
                        controller.scenarios = data;
                    });
                $location.path("/scenario/");
            };

            this.getTerminationConditionOfScenario = function(id) {
                $http.get(JEngine_Server_URL + "/" + JCore_REST_Interface + "/scenario/" + id + "/terminationcondition/").
                    success(function(data) {
                        controller.currentScenario['terminationcondition'] = data;
                    }).
                    error(function() {
                        console.log('request failed');
                    });
            };
			
			// Creates a new instance of the scenario with the given Id
			this.createInstance = function(id){
				$http.post(JEngine_Server_URL + "/" + JCore_REST_Interface + "/scenario/" + id + "/instance/").
					success(function(data) {
						$location.path("/scenario/" + id + "/instance/" + data['id']);
					}).
                    error(function() {
                        console.log('request failed');
                    });
			};

            /* ____ BEGIN_INITIALIZATION ____ */
            this.initialize = function(){
                console.log('initializing...');
            }

            this.initialize();
            /* ____ END_INITIALIZATION ____ */
		}]
	);
	
	userIn.controller('ScenarioInstanceController', ['$routeParams', '$location', '$http', '$scope',
		function($routeParams, $location, $http){
			// For accessing data from inside the $http context
			var instanceCtrl = this;
			
			// initialize an empty object for the instances
			this.instances = {};
            this.instanceDetails = {};
			this.scenario = {};

            this.activityOutput = {};

            //post update for webservice tasks
            this.submitAttributeForm=function(){
                var data=$scope.form;
                //TODO: edit path
                $http.put(JEngine_Server_URL + "/" + JConfig_REST_Interface + "/webservice/"+ webserviceC.workingID + "/?", data);
            }

			/* ____ BEGIN_INITIALIZATION ____ */
			this.initializeActivityInstances = function(){
				instanceCtrl.instanceDetails.activities = {};
				["ready", "terminated", "running"].forEach(function(state){
					var state2 = state;
					$http.get(
						JEngine_Server_URL + "/" + JCore_REST_Interface + "/scenario/" +
						$routeParams.id + "/instance/" + $routeParams.instanceId + "/activity/?state=" + state).
					success(function(data){
						instanceCtrl.instanceDetails.activities[state] = data;
					}).
                        error(function() {
                            console.log('request failed');
                        });
				});
			}

			this.initializeDataobjectInstances = function(){
				instanceCtrl.instanceDetails.dataobjects = {};
					$http.get(JEngine_Server_URL + "/" + JCore_REST_Interface +
						  "/scenario/" + $routeParams.id + "/instance/" + $routeParams.instanceId +
						"/dataobject/").
					success(function(data){
						instanceCtrl.instanceDetails.dataobjects = data;
					}).
                        error(function() {
                            console.log('request failed');
                        });
			}
			// activitylogs
			this.initializeActivitylogInstances = function(){
				instanceCtrl.instanceDetails.dataobjects = {};
					$http.get(JEngine_Server_URL + "/" + JHistory_REST_Interface +
						"/scenario/" + $routeParams.id + "/instance/" + $routeParams.instanceId +
						"/activities/").
					success(function(data){
						instanceCtrl.instanceDetails.activitylogs = data;
					}).
                        error(function() {
                            console.log('request failed');
                        });
			}
			// dataobjectlogs
			this.initializeDataobjectlogInstances = function(){
				instanceCtrl.instanceDetails.dataobjects = {};
					$http.get(JEngine_Server_URL + "/" + JHistory_REST_Interface +
						"/scenario/" + $routeParams.id + "/instance/" + $routeParams.instanceId +
						"/dataobjects/").
					success(function(data){
						instanceCtrl.instanceDetails.dataobjectlogs = data;
					}).
                        error(function() {
                            console.log('request failed');
                        });
			}

            // dataobjectattributeslogs
            this.initializeDataobjectAttributelogInstances = function(){
                instanceCtrl.instanceDetails.dataobjects = {};
                $http.get(JEngine_Server_URL + "/" + JHistory_REST_Interface +
                "/scenario/" + $routeParams.id + "/instance/" + $routeParams.instanceId +
                "/attributes/").
                    success(function(data){
                        instanceCtrl.instanceDetails.dataobjectAttributelogs = data;
                    }).
                    error(function() {
                        console.log('request failed');
                    });
            }
            // if necessary initialize the specified Scenario


            this.initialize = function(){
                if ($routeParams.instanceId) {
                    // initialize if necessary the specified instance
                    // The scenario and instance is specified by the routeParams
                    this.initializeInstance($routeParams.instanceId);
                } else {
                    // First get the Scenario InstanceIds
                    $http.get(JEngine_Server_URL + "/" + JCore_REST_Interface + "/scenario/"+ $routeParams.id + "/instance/").
                        success(function(data) {
                            instanceCtrl.scenario['instances'] = data;
                            instanceCtrl.scenario['id'] = $routeParams.id;
                        }).error(function() {
                            console.log('request failed');
                        });
                }
            };

            // Initializes an Instance object for the given ID by fetching the information
            this.initializeInstance = function(id) {
                $http.get(
                    JEngine_Server_URL + "/" + JCore_REST_Interface +
                    "/scenario/" + $routeParams.id + "/instance/" +
                    id + "/"
                ).success(function (data) {
                        instanceCtrl.instanceDetails['id'] = id;
                        if ($routeParams.instanceId) {
                            instanceCtrl.initializeActivityInstances();
                            instanceCtrl.initializeDataobjectInstances();
                            instanceCtrl.initializeActivitylogInstances();
                            instanceCtrl.initializeDataobjectlogInstances();
                            instanceCtrl.initializeDataobjectAttributelogInstances();
                            instanceCtrl.getTerminationConditionOfScenario($routeParams.id);
                        }
                    }).
                    error(function () {
                        console.log('request failed');
                    });
            }

            this.initialize();
            /* ____ END_INITIALIZATION ____ */
			
			// Got to the instance with the given Id
			this.goToDetailsFrom = function(id){
				$location.path("/scenario/" + $routeParams.id + "/instance/" + id);
			};
			
			// returns the current instance object
			this.getCurrentInstance = function(){
                instanceCtrl.instanceDetails['id'] = $routeParams.instanceId;
			};
			
			// begins an activity
			this.beginActivity = function(activityId) {
				var dataObject  = "";
				$http.put(JEngine_Server_URL + "/" + JCore_REST_Interface +
					"/scenario/" + $routeParams.id + "/instance/" + $routeParams.instanceId +
					"/activity/"+ activityId + "?state=begin", dataObject).
					success(function(data) {
						instanceCtrl.instanceDetails.activities = {};
                        //reloading content so the dashboard is uptodate
						instanceCtrl.initializeActivityInstances();
						instanceCtrl.initializeDataobjectInstances();
						instanceCtrl.initializeActivitylogInstances();
						instanceCtrl.initializeDataobjectlogInstances();
                        instanceCtrl.initializeDataobjectAttributelogInstances();
					}).
                    error(function() {
                        console.log('request failed');
                    });
			};
			
			// terminates an activity
			this.terminateActivity = function(activityId) {
				var dataObject = "";
				$http.put(JEngine_Server_URL + "/" + JCore_REST_Interface +
					"/scenario/" + $routeParams.id + "/instance/" + $routeParams.instanceId +
					"/activity/"+ activityId + "?state=terminate", dataObject).
					success(function(data) {
						instanceCtrl.instanceDetails.activities = {};
                        //reloading content so the dashboard is uptodate
						instanceCtrl.initializeActivityInstances();
						instanceCtrl.initializeDataobjectInstances();
						instanceCtrl.initializeActivitylogInstances();
						instanceCtrl.initializeDataobjectlogInstances();
                        instanceCtrl.initializeDataobjectAttributelogInstances();
					}).
                    error(function() {
                        console.log('request failed');
                    });
			};

            this.getTerminationConditionOfScenario = function(id) {
                $http.get(JEngine_Server_URL + "/" + JCore_REST_Interface + "/scenario/" + id + "/terminationcondition/").
                    success(function(data) {
                        instanceCtrl.scenario['terminationcondition'] = data;
                    }).
                    error(function() {
                        console.log('request failed');
                    });
            };

            this.getActivityReferences = function(activityID) {
                $http.get(JEngine_Server_URL + "/" + JCore_REST_Interface + "/scenario/" + $routeParams.id + "/instance/" + $routeParams.instanceId + "/activity/" + activityID + "/references").
                    success(function(data) {
                        instanceCtrl.scenario['activity'][activityID]['references'] = data;
                    }).
                    error(function() {
                        console.log('request failed');
                    });
            };

            this.getActivityOutput = function(activityID) {
                $http.get(JEngine_Server_URL + "/" + JCore_REST_Interface + "/scenario/" + $routeParams.id + "/instance/" + $routeParams.instanceId + "/activity/" + activityID + "/output").
                    success(function(data) {
                        instanceCtrl.scenario['activity'][activityID]['output'] = data;
                    }).
                    error(function() {
                        console.log('request failed');
                    });
            };

            this.getOutputsets = function(outputsetID, activityID) {
                $http.get(JEngine_Server_URL + "/" + JCore_REST_Interface + "/scenario/" + $routeParams.id + "/instance/" + $routeParams.instanceId + "/outputset/" + outputsetID + "").
                    success(function(data) {
                        instanceCtrl.scenario['activity'][activityID]['outputsets'][outputsetID] = data;
                    }).
                    error(function() {
                        console.log('request failed');
                    });
            };

            this.getOutputAndOutputsets = function(activityID) {
                if(!instanceCtrl.scenario['outputsets']){
                    instanceCtrl.scenario['outputsets'] = {};
                }
                //retrieving the output for each retrieved referenced Activity
                $http.get(JEngine_Server_URL + "/" + JCore_REST_Interface + "/scenario/" + $routeParams.id + "/instance/" + $routeParams.instanceId + "/activity/" + activityID + "/output").
                    success(function(data2) {
                        instanceCtrl.activityOutput[activityID] = {};
                        instanceCtrl.activityOutput[activityID] = data2;
                        //now, we also want to get the details of the outputset to access the label e.g. for each entry
                        angular.forEach(instanceCtrl.activityOutput[activityID], function(outputset, key2) {
                            $http.get(JEngine_Server_URL + "/" + JCore_REST_Interface + "/scenario/" + $routeParams.id + "/instance/" + $routeParams.instanceId + "/outputset/" + outputset['id'] + "").
                                success(function(data3) {
                                    instanceCtrl.scenario['outputsets'][outputset['id']] = {};
                                    instanceCtrl.scenario['outputsets'][outputset['id']] = data3;
                                }).
                                error(function() {
                                    console.log('request failed');
                                });
                        });
                    }).
                    error(function() {
                        console.log('request failed');
                    });
            };

            this.handleReferencedActivities = function(activityID) {
                instanceCtrl.scenario['outputsets'] = {};
                instanceCtrl.scenario['activity'] = {};
                instanceCtrl.scenario['activity'][activityID] = {};
                //retrieve referenced Activities for this activityID
                $http.get(JEngine_Server_URL + "/" + JCore_REST_Interface + "/scenario/" + $routeParams.id + "/instance/" + $routeParams.instanceId + "/activity/" + activityID + "/references").
                    success(function(data) {
                        instanceCtrl.scenario['activity'][activityID]['references'] = data;
                        var activityArray = data['ids'];
                        activityArray.push(activityID);
                        //check if there are any referenced Activities
                        if(instanceCtrl.scenario['activity'][activityID]['references']['ids'].length > 0){
                            //if so, lets get the output for each of them
                            angular.forEach(activityArray, function(refActivityID, key) {
                                //retrieving the output for each retrieved referenced Activity
                                $http.get(JEngine_Server_URL + "/" + JCore_REST_Interface + "/scenario/" + $routeParams.id + "/instance/" + $routeParams.instanceId + "/activity/" + refActivityID + "/output").
                                    success(function(data2) {
                                        instanceCtrl.activityOutput[refActivityID] = {};
                                        instanceCtrl.activityOutput[refActivityID] = data2;
                                        //now, we also want to get the details of the outputset to access the label e.g. for each entry
                                        angular.forEach(instanceCtrl.activityOutput[refActivityID], function(outputset, key2) {
                                            $http.get(JEngine_Server_URL + "/" + JCore_REST_Interface + "/scenario/" + $routeParams.id + "/instance/" + $routeParams.instanceId + "/outputset/" + outputset['id'] + "").
                                                success(function(data3) {
                                                    instanceCtrl.scenario['outputsets'][outputset['id']] = {};
                                                    instanceCtrl.scenario['outputsets'][outputset['id']] = data3;
                                                }).
                                                error(function() {
                                                    console.log('request failed');
                                                });
                                        });
                                    }).
                                    error(function() {
                                        console.log('request failed');
                                    });
                            });
                        }
                    }).
                    error(function() {
                        console.log('request failed');
                    });
            };

		}
	]);
})();
