(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('param-execution-job', {
            parent: 'entity',
            url: '/param-execution-job',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.paramExecutionJob.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/param-execution-job/param-execution-jobs.html',
                    controller: 'ParamExecutionJobController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('paramExecutionJob');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('param-execution-job-detail', {
            parent: 'param-execution-job',
            url: '/param-execution-job/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.paramExecutionJob.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/param-execution-job/param-execution-job-detail.html',
                    controller: 'ParamExecutionJobDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('paramExecutionJob');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'ParamExecutionJob', function($stateParams, ParamExecutionJob) {
                    return ParamExecutionJob.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'param-execution-job',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('param-execution-job-detail.edit', {
            parent: 'param-execution-job-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/param-execution-job/param-execution-job-dialog.html',
                    controller: 'ParamExecutionJobDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['ParamExecutionJob', function(ParamExecutionJob) {
                            return ParamExecutionJob.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('param-execution-job.new', {
            parent: 'param-execution-job',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/param-execution-job/param-execution-job-dialog.html',
                    controller: 'ParamExecutionJobDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                cle: null,
                                valeur: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('param-execution-job', null, { reload: 'param-execution-job' });
                }, function() {
                    $state.go('param-execution-job');
                });
            }]
        })
        .state('param-execution-job.edit', {
            parent: 'param-execution-job',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/param-execution-job/param-execution-job-dialog.html',
                    controller: 'ParamExecutionJobDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['ParamExecutionJob', function(ParamExecutionJob) {
                            return ParamExecutionJob.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('param-execution-job', null, { reload: 'param-execution-job' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('param-execution-job.delete', {
            parent: 'param-execution-job',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/param-execution-job/param-execution-job-delete-dialog.html',
                    controller: 'ParamExecutionJobDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['ParamExecutionJob', function(ParamExecutionJob) {
                            return ParamExecutionJob.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('param-execution-job', null, { reload: 'param-execution-job' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
