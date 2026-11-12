(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('emetteur', {
            parent: 'entity',
            url: '/emetteur?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.emetteur.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/emetteur/emetteurs.html',
                    controller: 'EmetteurController',
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
                    $translatePartialLoader.addPart('emetteur');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('emetteur-detail', {
            parent: 'emetteur',
            url: '/emetteur/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.emetteur.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/emetteur/emetteur-detail.html',
                    controller: 'EmetteurDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('emetteur');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Emetteur', function($stateParams, Emetteur) {
                    return Emetteur.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'emetteur',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('emetteur-detail.edit', {
            parent: 'emetteur-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/emetteur/emetteur-dialog.html',
                    controller: 'EmetteurDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Emetteur', function(Emetteur) {
                            return Emetteur.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('emetteur.new', {
            parent: 'emetteur',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/emetteur/emetteur-dialog.html',
                    controller: 'EmetteurDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                code: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('emetteur', null, { reload: 'emetteur' });
                }, function() {
                    $state.go('emetteur');
                });
            }]
        })
        .state('emetteur.edit', {
            parent: 'emetteur',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/emetteur/emetteur-dialog.html',
                    controller: 'EmetteurDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Emetteur', function(Emetteur) {
                            return Emetteur.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('emetteur', null, { reload: 'emetteur' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('emetteur.delete', {
            parent: 'emetteur',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/emetteur/emetteur-delete-dialog.html',
                    controller: 'EmetteurDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Emetteur', function(Emetteur) {
                            return Emetteur.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('emetteur', null, { reload: 'emetteur' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
