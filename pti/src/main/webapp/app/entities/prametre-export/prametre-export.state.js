(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('prametre-export', {
            parent: 'entity',
            url: '/prametre-export?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.prametreExport.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/prametre-export/prametre-exports.html',
                    controller: 'PrametreExportController',
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
                PreviousState: ["$state", function ($state) {
                    var currentStateData = {
                        Name: $state.current.name,
                        Params: angular.copy($state.params),
                        URL: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }],
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
                    $translatePartialLoader.addPart('prametreExport');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('prametre-export-detail', {
            parent: 'prametre-export',
            url: '/prametre-export/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.prametreExport.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/prametre-export/prametre-export-detail.html',
                    controller: 'PrametreExportDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('prametreExport');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'PrametreExport', function($stateParams, PrametreExport) {
                    return PrametreExport.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'prametre-export',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('prametre-export-detail.edit', {
            parent: 'prametre-export-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/prametre-export/prametre-export-dialog.html',
                    controller: 'PrametreExportDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['PrametreExport', function(PrametreExport) {
                            return PrametreExport.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('prametre-export.new', {
            parent: 'prametre-export',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/prametre-export/prametre-export-dialog.html',
                    controller: 'PrametreExportDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: function () {
                            return {
                                nomParametre: null,
                                exportTable: 4,

                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('prametre-export', null, { reload: 'prametre-export' });
                }, function() {
                    $state.go('prametre-export');
                });
            }]
        })
        .state('prametre-export.edit', {
            parent: 'prametre-export',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/prametre-export/prametre-export-dialog.html',
                    controller: 'PrametreExportDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['PrametreExport', function(PrametreExport) {
                            return PrametreExport.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    // $state.go('prametre-export', null, { reload: 'prametre-export' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('prametre-export.delete', {
            parent: 'prametre-export',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/prametre-export/prametre-export-delete-dialog.html',
                    controller: 'PrametreExportDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['PrametreExport', function(PrametreExport) {
                            return PrametreExport.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('prametre-export', null, { reload: 'prametre-export' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
