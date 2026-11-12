(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('export-table', {
            parent: 'entity',
            url: '/export-table',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.exportTable.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/export-table/export-tables.html',
                    controller: 'ExportTableController',
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
                    $translatePartialLoader.addPart('exportTable');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('export-table-detail', {
            parent: 'export-table',
            url: '/export-table/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.exportTable.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/export-table/export-table-detail.html',
                    controller: 'ExportTableDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('exportTable');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'ExportTable', function($stateParams, ExportTable) {
                    return ExportTable.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'export-table',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('export-table-detail.edit', {
            parent: 'export-table-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/export-table/export-table-dialog.html',
                    controller: 'ExportTableDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['ExportTable', function(ExportTable) {
                            return ExportTable.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('export-table.new', {
            parent: 'export-table',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/export-table/export-table-dialog.html',
                    controller: 'ExportTableDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: function () {
                            return {
                                nomExport: null,
                                description: null,
                                outputFilename: null,
                                libelle:null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('export-table', null, { reload: 'export-table' });
                }, function() {
                    $state.go('export-table');
                });
            }]
        })
        .state('export-table.edit', {
            parent: 'export-table',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/export-table/export-table-dialog.html',
                    controller: 'ExportTableDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['ExportTable', function(ExportTable) {
                            return ExportTable.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('export-table', null, { reload: 'export-table' });
                }, function() {
                    $state.go('^');
                });
            }]
        })

        .state('export-table.delete', {
            parent: 'export-table',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/export-table/export-table-delete-dialog.html',
                    controller: 'ExportTableDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['ExportTable', function(ExportTable) {
                            return ExportTable.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('export-table', null, { reload: 'export-table' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
