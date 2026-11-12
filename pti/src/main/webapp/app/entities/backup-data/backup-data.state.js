(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('backup-data', {
            parent: 'entity',
            url: '/backup-data?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.serviceProfil.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/backup-data/backup-data.html',
                    controller: 'BackupDataController',
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
                    $translatePartialLoader.addPart('serviceProfil');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('backup-data.new', {
            parent: 'backup-data',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/backup-data/backup-data-dialog.html',
                    controller: 'BackupDataDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: function () {
                            return {
                                actionService: null,
                                id: null,
                                typeService:null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('backup-data', null, { reload: 'backup-data' });
                }, function() {
                    $state.go('backup-data');
                });
            }]
        })
        .state('backup-data.edit', {
            parent: 'backup-data',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/backup-data/backup-data-dialog.html',
                    controller: 'BackupDataDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['BackupData', function(BackupData) {
                            return BackupData.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('backup-data', null, { reload: 'backup-data' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('backup-data.delete', {
            parent: 'backup-data',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/backup-data/backup-data-delete-dialog.html',
                    controller: 'BackupDataDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['BackupData', function(BackupData) {
                            return BackupData.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('backup-data', null, { reload: 'backup-data' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
