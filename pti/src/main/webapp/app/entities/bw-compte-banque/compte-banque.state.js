(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('compte-banque', {
            parent: 'entity',
            url: '/compte-banque?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: ''
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/bw-compte-banque/compte-banques.html',
                    controller: 'CompteBanqueController',
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
                //    $translatePartialLoader.addPart('compteBanque');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('compte-banque-detail', {
            parent: 'compte-banque',
            url: '/compte-banque/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: ''
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/bw-compte-banque/compte-banque-detail.html',
                    controller: 'CompteBanqueDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('compteBanque');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'CompteBanque', function($stateParams, CompteBanque) {
                    return CompteBanque.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'compte-banque',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('compte-banque-detail.edit', {
            parent: 'compte-banque-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/bw-compte-banque/compte-banque-dialog.html',
                    controller: 'CompteBanqueDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['CompteBanque', function(CompteBanque) {
                            return CompteBanque.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('compte-banque.new', {
            parent: 'compte-banque',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/bw-compte-banque/compte-banque-dialog.html',
                    controller: 'CompteBanqueDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                codeAgence: null,
                                codeBanque: null,
                                codeFse: null,
                                rib: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('compte-banque', null, { reload: 'compte-banque' });
                }, function() {
                    $state.go('compte-banque');
                });
            }]
        })
        .state('compte-banque.edit', {
            parent: 'compte-banque',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/bw-compte-banque/compte-banque-dialog.html',
                    controller: 'CompteBanqueDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['CompteBanque', function(CompteBanque) {
                            return CompteBanque.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('compte-banque', null, { reload: 'compte-banque' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('compte-banque.delete', {
            parent: 'compte-banque',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/bw-compte-banque/compte-banque-delete-dialog.html',
                    controller: 'CompteBanqueDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['CompteBanque', function(CompteBanque) {
                            return CompteBanque.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('compte-banque', null, { reload: 'compte-banque' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
