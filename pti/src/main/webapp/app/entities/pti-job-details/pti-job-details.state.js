(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('pti-job-details', {
            parent: 'entity',
            url: '/pti-job-details?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.ptiJobDetails.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/pti-job-details/pti-job-details.html',
                    controller: 'PtiJobDetailsController',
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
                    $translatePartialLoader.addPart('ptiJobDetails');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('pti-job-details-detail', {
            parent: 'pti-job-details',
            url: '/pti-job-details/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.ptiJobDetails.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/pti-job-details/pti-job-details-detail.html',
                    controller: 'PtiJobDetailsDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('ptiJobDetails');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'PtiJobDetails', function($stateParams, PtiJobDetails) {
                    return PtiJobDetails.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'pti-job-details',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('pti-job-details-detail.edit', {
            parent: 'pti-job-details-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/pti-job-details/pti-job-details-dialog.html',
                    controller: 'PtiJobDetailsDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['PtiJobDetails', function(PtiJobDetails) {
                            return PtiJobDetails.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('pti-job-details.new', {
            parent: 'pti-job-details',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/pti-job-details/pti-job-details-dialog.html',
                    controller: 'PtiJobDetailsDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: function () {
                            return {
                                jobName: null,
                                displayName: null,
                                expressionCron: null,
                                priorite: null,
                                schedulerGroupe: null,
                                nomGroupe: null,
                                dateDerniereExecution: null,
                                dateProchaineExecution: null,
                                jobKey: null,
                                erreurExecution: null,
                                estActif: null,
                                estEnExecution: null,
                                fournisseurService: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('pti-job-details', null, { reload: 'pti-job-details' });
                }, function() {
                    $state.go('pti-job-details');
                });
            }]
        })
        .state('pti-job-details.edit', {
            parent: 'pti-job-details',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/pti-job-details/pti-job-details-dialog.html',
                    controller: 'PtiJobDetailsDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['PtiJobDetails', function(PtiJobDetails) {
                            return PtiJobDetails.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('pti-job-details', null, { reload: 'pti-job-details' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('pti-job-details.delete', {
            parent: 'pti-job-details',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/pti-job-details/pti-job-details-delete-dialog.html',
                    controller: 'PtiJobDetailsDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['PtiJobDetails', function(PtiJobDetails) {
                            return PtiJobDetails.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('pti-job-details', null, { reload: 'pti-job-details' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
