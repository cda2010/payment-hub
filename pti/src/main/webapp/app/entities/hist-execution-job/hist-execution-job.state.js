(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('hist-execution-job', {
            parent: 'entity',
            url: '/hist-execution-job?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.histExecutionJob.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/hist-execution-job/hist-execution-jobs.html',
                    controller: 'HistExecutionJobController',
                    controllerAs: 'vm'
                }
            },
            params: {
                page: {
                    value: '1',
                    squash: true
                },
                sort: {
                    value: 'id,asc',
                    squash: true
                },
                search: null
            },
            resolve: {
                pagingParams: ['$stateParams', 'PaginationUtil', function ($stateParams, PaginationUtil) {
                    return {
                        page: PaginationUtil.parsePage($stateParams.page),
                        sort: $stateParams.sort,
                        predicate: PaginationUtil.parsePredicate($stateParams.sort),
                        ascending: PaginationUtil.parseAscending($stateParams.sort),
                        search: $stateParams.search
                    };
                }],
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('histExecutionJob');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('hist-execution-job-detail', {
            parent: 'hist-execution-job',
            url: '/hist-execution-job/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.histExecutionJob.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/hist-execution-job/hist-execution-job-detail.html',
                    controller: 'HistExecutionJobDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('histExecutionJob');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'HistExecutionJob', function($stateParams, HistExecutionJob) {
                    return HistExecutionJob.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'hist-execution-job',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('hist-execution-job-detail.edit', {
            parent: 'hist-execution-job-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/hist-execution-job/hist-execution-job-dialog.html',
                    controller: 'HistExecutionJobDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['HistExecutionJob', function(HistExecutionJob) {
                            return HistExecutionJob.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('hist-execution-job.new', {
            parent: 'hist-execution-job',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/hist-execution-job/hist-execution-job-dialog.html',
                    controller: 'HistExecutionJobDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                typeTrigger: null,
                                debutExecution: null,
                                finExecution: null,
                                statut: null,
                                messageErreur: null,
                                erreurLog: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('hist-execution-job', null, { reload: 'hist-execution-job' });
                }, function() {
                    $state.go('hist-execution-job');
                });
            }]
        })
        .state('hist-execution-job.edit', {
            parent: 'hist-execution-job',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/hist-execution-job/hist-execution-job-dialog.html',
                    controller: 'HistExecutionJobDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['HistExecutionJob', function(HistExecutionJob) {
                            return HistExecutionJob.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('hist-execution-job', null, { reload: 'hist-execution-job' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('hist-execution-job.delete', {
            parent: 'hist-execution-job',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/hist-execution-job/hist-execution-job-delete-dialog.html',
                    controller: 'HistExecutionJobDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['HistExecutionJob', function(HistExecutionJob) {
                            return HistExecutionJob.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('hist-execution-job', null, { reload: 'hist-execution-job' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
