(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('personne-physique', {
            parent: 'entity',
            url: '/personne-physique?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.personnePhysique.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/personne-physique/personne-physiques.html',
                    controller: 'PersonnePhysiqueController',
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
                    $translatePartialLoader.addPart('personnePhysique');
                    $translatePartialLoader.addPart('sexe');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('personne-physique-detail', {
            parent: 'personne-physique',
            url: '/personne-physique/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.personnePhysique.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/personne-physique/personne-physique-detail.html',
                    controller: 'PersonnePhysiqueDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('personnePhysique');
                    $translatePartialLoader.addPart('sexe');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'PersonnePhysique', function($stateParams, PersonnePhysique) {
                    return PersonnePhysique.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'personne-physique',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('personne-physique-detail.edit', {
            parent: 'personne-physique-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/personne-physique/personne-physique-dialog.html',
                    controller: 'PersonnePhysiqueDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['PersonnePhysique', function(PersonnePhysique) {
                            return PersonnePhysique.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('personne-physique.new', {
            parent: 'personne-physique',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/personne-physique/personne-physique-dialog.html',
                    controller: 'PersonnePhysiqueDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                nomClient: null,
                                prenomClient: null,
                                dateNaissance: null,
                                personnePrevenir: null,
                                lieuNaissance: null,
                                sexe: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('personne-physique', null, { reload: 'personne-physique' });
                }, function() {
                    $state.go('personne-physique');
                });
            }]
        })
        .state('personne-physique.edit', {
            parent: 'personne-physique',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/personne-physique/personne-physique-dialog.html',
                    controller: 'PersonnePhysiqueDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['PersonnePhysique', function(PersonnePhysique) {
                            return PersonnePhysique.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('personne-physique', null, { reload: 'personne-physique' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('personne-physique.delete', {
            parent: 'personne-physique',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/personne-physique/personne-physique-delete-dialog.html',
                    controller: 'PersonnePhysiqueDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['PersonnePhysique', function(PersonnePhysique) {
                            return PersonnePhysique.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('personne-physique', null, { reload: 'personne-physique' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
