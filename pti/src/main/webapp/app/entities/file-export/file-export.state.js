(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('file-export', {
            parent: 'entity',
            url: '/file-export',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.fileExport.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/file-export/file-export.html',
                    controller: 'FileExportController',
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
                    $translatePartialLoader.addPart('fileExport');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })

        .state('file-export.new', {
            parent: 'file-export',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/file-export/file-export-dialog.html',
                    controller: 'FileExportDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                fileName: null,
                                exportTable: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('file-export', null, { reload: 'file-export' });
                }, function() {
                    $state.go('file-export');
                });
            }]
        })

        .state('file-export.edit', {
            parent: 'file-export',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/file-export/file-export-dialog.html',
                    controller: 'FileExportDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['FileExport', function(FileExport) {
                            return FileExport.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('file-export', null, { reload: 'file-export' });
                }, function() {
                    $state.go('^');
                });
            }]
        })

        .state('file-export.delete', {
            parent: 'file-export',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/file-export/file-export-delete-dialog.html',
                    controller: 'FileExportDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['FileExport', function(FileExport) {
                            return FileExport.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('file-export', null, { reload: 'file-export' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
