(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('ftp', {
            parent: 'entity',
            url: '/ftp?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.ftp.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/ftp/ftps.html',
                    controller: 'FtpController',
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
                    $translatePartialLoader.addPart('ftp');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('ftp-detail', {
            parent: 'ftp',
            url: '/ftp/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.ftp.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/ftp/ftp-detail.html',
                    controller: 'FtpDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('ftp');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Ftp', function($stateParams, Ftp) {
                    return Ftp.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'ftp',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('ftp-detail.edit', {
            parent: 'ftp-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/ftp/ftp-dialog.html',
                    controller: 'FtpDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Ftp', function(Ftp) {
                            return Ftp.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('ftp.new', {
            parent: 'ftp',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/ftp/ftp-dialog.html',
                    controller: 'FtpDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                url: null,
                                username: null,
                                password: null,
                                repertoire: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('ftp', null, { reload: 'ftp' });
                }, function() {
                    $state.go('ftp');
                });
            }]
        })
        .state('ftp.edit', {
            parent: 'ftp',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/ftp/ftp-dialog.html',
                    controller: 'FtpDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Ftp', function(Ftp) {
                            return Ftp.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('ftp', null, { reload: 'ftp' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('ftp.delete', {
            parent: 'ftp',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/ftp/ftp-delete-dialog.html',
                    controller: 'FtpDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Ftp', function(Ftp) {
                            return Ftp.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('ftp', null, { reload: 'ftp' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
