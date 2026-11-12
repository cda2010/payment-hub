(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('groupe-profil-acteur', {
            parent: 'entity',
            url: '/groupe-profil-acteur?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.groupeProfilActeur.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/groupe-profil-acteur/groupe-profil-acteurs.html',
                    controller: 'GroupeProfilActeurController',
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
                    $translatePartialLoader.addPart('groupeProfilActeur');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('groupe-profil-acteur-detail', {
            parent: 'groupe-profil-acteur',
            url: '/groupe-profil-acteur/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.groupeProfilActeur.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/groupe-profil-acteur/groupe-profil-acteur-detail.html',
                    controller: 'GroupeProfilActeurDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('groupeProfilActeur');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'GroupeProfilActeur', function($stateParams, GroupeProfilActeur) {
                    return GroupeProfilActeur.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'groupe-profil-acteur',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('groupe-profil-acteur-detail.edit', {
            parent: 'groupe-profil-acteur-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/groupe-profil-acteur/groupe-profil-acteur-dialog.html',
                    controller: 'GroupeProfilActeurDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['GroupeProfilActeur', function(GroupeProfilActeur) {
                            return GroupeProfilActeur.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('groupe-profil-acteur.new', {
            parent: 'groupe-profil-acteur',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/groupe-profil-acteur/groupe-profil-acteur-dialog.html',
                    controller: 'GroupeProfilActeurDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: function () {
                            return {
                                nom: null,
                                description: null,
                                type: null,
                                destineA: null,
                                codeFSE: null,
                                active: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('groupe-profil-acteur', null, { reload: 'groupe-profil-acteur' });
                }, function() {
                    $state.go('groupe-profil-acteur');
                });
            }]
        })
        .state('groupe-profil-acteur.edit', {
            parent: 'groupe-profil-acteur',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/groupe-profil-acteur/groupe-profil-acteur-dialog.html',
                    controller: 'GroupeProfilActeurDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['GroupeProfilActeur', function(GroupeProfilActeur) {
                            return GroupeProfilActeur.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('groupe-profil-acteur', null, { reload: 'groupe-profil-acteur' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('groupe-profil-acteur.delete', {
            parent: 'groupe-profil-acteur',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/groupe-profil-acteur/groupe-profil-acteur-delete-dialog.html',
                    controller: 'GroupeProfilActeurDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['GroupeProfilActeur', function(GroupeProfilActeur) {
                            return GroupeProfilActeur.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('groupe-profil-acteur', null, { reload: 'groupe-profil-acteur' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
