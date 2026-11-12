(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('utilitaire', {
            parent: 'entity',
            url: '/utilitaire?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.utilitaire.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/utilitaire/utilitaires.html',
                    controller: 'UtilitaireController',
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
                    $translatePartialLoader.addPart('utilitaire');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('utilitaire-detail', {
            parent: 'utilitaire',
            url: '/utilitaire/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.utilitaire.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/utilitaire/utilitaire-detail.html',
                    controller: 'UtilitaireDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('utilitaire');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Utilitaire', function($stateParams, Utilitaire) {
                    return Utilitaire.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'utilitaire',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('utilitaire-detail.edit', {
            parent: 'utilitaire-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/utilitaire/utilitaire-dialog.html',
                    controller: 'UtilitaireDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Utilitaire', function(Utilitaire) {
                            return Utilitaire.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('utilitaire.new', {
            parent: 'utilitaire',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/utilitaire/utilitaire-dialog.html',
                    controller: 'UtilitaireDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                nom: null,
                                description: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('utilitaire', null, { reload: 'utilitaire' });
                }, function() {
                    $state.go('utilitaire');
                });
            }]
        })
        .state('utilitaire.edit', {
            parent: 'utilitaire',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/utilitaire/utilitaire-dialog.html',
                    controller: 'UtilitaireDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Utilitaire', function(Utilitaire) {
                            return Utilitaire.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('utilitaire', null, { reload: 'utilitaire' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('utilitaire.delete', {
            parent: 'utilitaire',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/utilitaire/utilitaire-delete-dialog.html',
                    controller: 'UtilitaireDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Utilitaire', function(Utilitaire) {
                            return Utilitaire.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('utilitaire', null, { reload: 'utilitaire' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
