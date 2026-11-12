(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('bw-account', {
            parent: 'entity',
            url: '/bw-account?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.bwAccount.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/bw-account/bw-accounts.html',
                    controller: 'BwAccountController',
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
                    $translatePartialLoader.addPart('bwAccount');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('bw-account-detail', {
            parent: 'bw-account',
            url: '/bw-account/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.bwAccount.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/bw-account/bw-account-detail.html',
                    controller: 'BwAccountDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('bwAccount');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'BwAccount', function($stateParams, BwAccount) {
                    return BwAccount.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'bw-account',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('bw-account-detail.edit', {
            parent: 'bw-account-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/bw-account/bw-account-dialog.html',
                    controller: 'BwAccountDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['BwAccount', function(BwAccount) {
                            return BwAccount.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('bw-account.new', {
            parent: 'bw-account',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/bw-account/bw-account-dialog.html',
                    controller: 'BwAccountDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                codeBanque: null,
                                codeAgence: null,
                                numeroCompte: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('bw-account', null, { reload: 'bw-account' });
                }, function() {
                    $state.go('bw-account');
                });
            }]
        })
        .state('bw-account.edit', {
            parent: 'bw-account',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/bw-account/bw-account-dialog.html',
                    controller: 'BwAccountDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['BwAccount', function(BwAccount) {
                            return BwAccount.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('bw-account', null, { reload: 'bw-account' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('bw-account.delete', {
            parent: 'bw-account',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/bw-account/bw-account-delete-dialog.html',
                    controller: 'BwAccountDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['BwAccount', function(BwAccount) {
                            return BwAccount.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('bw-account', null, { reload: 'bw-account' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
