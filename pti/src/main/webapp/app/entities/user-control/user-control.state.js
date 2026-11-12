(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('user-control', {
            parent: 'entity',
            url: '/user-control?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.userControl.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/user-control/user-controls.html',
                    controller: 'UserControlController',
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
                    $translatePartialLoader.addPart('userControl');
                    $translatePartialLoader.addPart('typeControl');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('user-control-detail', {
            parent: 'user-control',
            url: '/user-control/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.userControl.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/user-control/user-control-detail.html',
                    controller: 'UserControlDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('userControl');
                    $translatePartialLoader.addPart('typeControl');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'UserControl', function($stateParams, UserControl) {
                    return UserControl.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'user-control',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('user-control-detail.edit', {
            parent: 'user-control-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/user-control/user-control-dialog.html',
                    controller: 'UserControlDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['UserControl', function(UserControl) {
                            return UserControl.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('user-control.new', {
            parent: 'user-control',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/user-control/user-control-dialog.html',
                    controller: 'UserControlDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: function () {
                            return {
                                action: null,
                                codePays: null,
                                login: null,
                                password: null,
                                typeActeur: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('user-control', null, { reload: 'user-control' });
                }, function() {
                    $state.go('user-control');
                });
            }]
        })
        .state('user-control.edit', {
            parent: 'user-control',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/user-control/user-control-dialog.html',
                    controller: 'UserControlDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['UserControl', function(UserControl) {
                            return UserControl.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('user-control', null, { reload: 'user-control' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('user-control.delete', {
            parent: 'user-control',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/user-control/user-control-delete-dialog.html',
                    controller: 'UserControlDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['UserControl', function(UserControl) {
                            return UserControl.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('user-control', null, { reload: 'user-control' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('etat-list-user', {
            parent: 'user-control',
            url: '/etat-list-user',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.userControl.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/user-control/user-etat.html',
                    controller: 'UserListController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('userControl');
                    $translatePartialLoader.addPart('typeControl');
                    return $translate.refresh();
                }]
            }
        });
    }

})();
