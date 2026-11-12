(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('compte', {
            parent: 'entity',
            url: '/compte?page&sort&search',
            data: {
                authorities: ['ROLE_CRE_COMPTE'],
                pageTitle: 'ptiApp.compte.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/compte/comptes.html',
                    controller: 'CompteController',
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
                    $translatePartialLoader.addPart('compte');
                    $translatePartialLoader.addPart('categorieCompte');
                    $translatePartialLoader.addPart('uniteOrganisation');
                    $translatePartialLoader.addPart('typeCompte');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })

            .state('compte-historique', {
                parent: 'entity',
                url: '/compte-historique',
                data: {
                    authorities: [],
                    pageTitle: 'ptiApp.compte.home.title'
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/entities/compte/compte-historique.html',
                        controller: 'CompteHistoriqueController',
                        controllerAs: 'vm'
                    }
                },
                params: {},
                resolve: {}
            })
        .state('compte.edit', {
            parent: 'compte',
            url: '/transactions-compte{id}',
            data: {
                authorities: ['ROLE_CONSULT_ALL_TRANSACTIONS']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/compte/compte-detail.html',
                    controller: 'CompteDetailController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Compte', function(Compte) {
                            return Compte.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('compte', null, { reload: 'compte' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('compte.delete', {
            parent: 'compte',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_CRE_COMPTE']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/compte/compte-delete-dialog.html',
                    controller: 'CompteDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Compte', function(Compte) {
                            return Compte.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('compte', null, { reload: 'compte' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('compte-en-instance', {
                parent: 'entity',
                url: '/compte-en-instance',
                data: {
                    authorities: [],
                    pageTitle: 'ptiApp.compte.home.title'
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/entities/compte/compte-en-instance.html',
                        controller: 'CompteInstanceController',
                        controllerAs: 'vm'
                    }
                },
                params: {},
                resolve: {}
            })
            .state('compte-inactif', {
                parent: 'entity',
                url: '/compte-inactif',
                data: {
                    authorities: [],
                    pageTitle: 'ptiApp.compte.home.title'
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/entities/compte/compte-inactif-pour-une-periode.html',
                        controller: 'CompteInactifPourUnePeriodeController',
                        controllerAs: 'vm'
                    }
                },
                params: {},
                resolve: {}
            });
    }

})();
