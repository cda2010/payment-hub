(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('releve-compte', {
            parent: 'entity',
            url: '/releve-compte?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.releveCompte.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/releve-compte/releve-comptes.html',
                    controller: 'ReleveCompteController',
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
                    $translatePartialLoader.addPart('releveCompte');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('releve-compte-detail', {
            parent: 'releve-compte',
            url: '/releve-compte/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.releveCompte.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/releve-compte/releve-compte-detail.html',
                    controller: 'ReleveCompteDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('releveCompte');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'ReleveCompte', function($stateParams, ReleveCompte) {
                    return ReleveCompte.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'releve-compte',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('releve-compte-detail.edit', {
            parent: 'releve-compte-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/releve-compte/releve-compte-dialog.html',
                    controller: 'ReleveCompteDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['ReleveCompte', function(ReleveCompte) {
                            return ReleveCompte.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('releve-compte.new', {
            parent: 'releve-compte',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/releve-compte/releve-compte-dialog.html',
                    controller: 'ReleveCompteDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                soldeInitial: null,
                                dateDebut: null,
                                soldeFinal: null,
                                dateFin: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('releve-compte', null, { reload: 'releve-compte' });
                }, function() {
                    $state.go('releve-compte');
                });
            }]
        })
        .state('releve-compte.edit', {
            parent: 'releve-compte',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/releve-compte/releve-compte-dialog.html',
                    controller: 'ReleveCompteDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['ReleveCompte', function(ReleveCompte) {
                            return ReleveCompte.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('releve-compte', null, { reload: 'releve-compte' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('releve-compte.delete', {
            parent: 'releve-compte',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/releve-compte/releve-compte-delete-dialog.html',
                    controller: 'ReleveCompteDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['ReleveCompte', function(ReleveCompte) {
                            return ReleveCompte.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('releve-compte', null, { reload: 'releve-compte' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
