(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('unite-organisation', {
            parent: 'entity',
            url: '/unite-organisation?page&sort&search',
            data: {
                authorities: ['ROLE_CRE_ORG'],
                pageTitle: 'ptiApp.uniteOrganisation.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/unite-organisation/unite-organisations.html',
                    controller: 'UniteOrganisationController',
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
                    $translatePartialLoader.addPart('uniteOrganisation');
                    $translatePartialLoader.addPart('personne');
                    $translatePartialLoader.addPart('typePersonne');
                    $translatePartialLoader.addPart('personnePhysique');
                    $translatePartialLoader.addPart('personneMorale');
                    $translatePartialLoader.addPart('sexe');
                    $translatePartialLoader.addPart('piece');
                      $translatePartialLoader.addPart('pointVente');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }],
                isConsult : function(){
                    return false;
                }
            }
        }).state('unite-organisation-paginer', {
            parent: 'entity',
            url: '/unite-organisation-paginer?page&sort&search',
            data: {
                authorities: ['ROLE_CRE_ORG','ROLE_CONSULT_ORG'],
                pageTitle: 'ptiApp.uniteOrganisation.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/unite-organisation/unite-organisations-paginer.html',
                    controller: 'UniteOrganisationPaginerController',
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
                    $translatePartialLoader.addPart('uniteOrganisation');
                    $translatePartialLoader.addPart('personne');
                    $translatePartialLoader.addPart('typePersonne');
                    $translatePartialLoader.addPart('personnePhysique');
                    $translatePartialLoader.addPart('personneMorale');
                    $translatePartialLoader.addPart('sexe');
                    $translatePartialLoader.addPart('piece');
                    $translatePartialLoader.addPart('pointVente');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }],
                isConsult : function(){
                    return false;
                }
            }
        })
            .state('consulter-unite-organisation', {
                parent: 'entity',
                url: '/consulter-unite-organisation?page&sort&search',
                data: {
                    authorities: ['ROLE_CONSULT_ORG'],
                    pageTitle: 'ptiApp.uniteOrganisation.home.title'
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/entities/unite-organisation/unite-organisations.html',
                        controller: 'UniteOrganisationController',
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
                        $translatePartialLoader.addPart('uniteOrganisation');
                        $translatePartialLoader.addPart('personne');
                        $translatePartialLoader.addPart('typePersonne');
                        $translatePartialLoader.addPart('personnePhysique');
                        $translatePartialLoader.addPart('personneMorale');
                        $translatePartialLoader.addPart('sexe');
                        $translatePartialLoader.addPart('piece');
                        $translatePartialLoader.addPart('pointVente');
                        $translatePartialLoader.addPart('global');
                        return $translate.refresh();
                    }],
                    isConsult : function(){
                        return true;
                    }
                }
            })
        .state('unite-organisation-detail', {
            parent: 'unite-organisation',
            url: '/unite-organisation/{id}',
            data: {
                authorities: ['ROLE_CONSULT_ORG'],
                pageTitle: 'ptiApp.uniteOrganisation.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/unite-organisation/unite-organisation-detail.html',
                    controller: 'UniteOrganisationDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('uniteOrganisation');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'UniteOrganisation', function($stateParams, UniteOrganisation) {
                    return UniteOrganisation.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'unite-organisation',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('unite-organisation-detail.edit', {
            parent: 'unite-organisation-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_CONSULT_ORG']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/unite-organisation/unite-organisation-detail.html',
                    controller: 'UniteOrganisationDetailController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['UniteOrganisation', function(UniteOrganisation) {
                            return UniteOrganisation.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('unite-organisation.new', {
            parent: 'unite-organisation',
            url: '/new',
            data: {
                authorities: ['ROLE_CRE_ORG']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/unite-organisation/unite-organisation-dialog.html',
                    controller: 'UniteOrganisationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                libelleUniteOrganisation: null,
                                estSiege: null,
                                codeUO: null,
                                numeroOrdre: null,
                                niveau: null,
                                feuille: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('unite-organisation', null, { reload: 'unite-organisation' });
                }, function() {
                    $state.go('unite-organisation');
                });
            }]
        })
        .state('unite-organisation.edit', {
            parent: 'unite-organisation',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_CRE_COMPTE']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/unite-organisation/unite-organisation-detail.html',
                    controller: 'UniteOrganisationDetailController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['UniteOrganisation', function(UniteOrganisation) {
                            return UniteOrganisation.get({id : $stateParams.id}).$promise;
                        }],
                    }
                }).result.then(function() {
                    $state.go('unite-organisation', null, { reload: 'unite-organisation' });
                }, function() {
                    $state.go('unite-organisation');
                });
            }]
        })
        .state('unite-organisation.delete', {
            parent: 'unite-organisation',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_CRE_ORG']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/unite-organisation/unite-organisation-delete-dialog.html',
                    controller: 'UniteOrganisationDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['UniteOrganisation', function(UniteOrganisation) {
                            return UniteOrganisation.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('unite-organisation', null, { reload: 'unite-organisation' });
                }, function() {
                    $state.go('^');
                });
            }]
        }).state('localisateur', {
            parent: 'entity',
            url: '/unite-organisation/localisateur',
            data: {
                authorities: ['ROLE_CONSULT_LOCALISATEUR'],
                pageTitle: 'ptiApp.uniteOrganisation.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/unite-organisation/localisateur/unite-org.html',
                    controller: 'LocalisateurController',
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
                    $translatePartialLoader.addPart('uniteOrganisation');
                    $translatePartialLoader.addPart('personne');
                    $translatePartialLoader.addPart('typePersonne');
                    $translatePartialLoader.addPart('personnePhysique');
                    $translatePartialLoader.addPart('personneMorale');
                    $translatePartialLoader.addPart('sexe');
                    $translatePartialLoader.addPart('piece');
                      $translatePartialLoader.addPart('pointVente');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }],
                isConsult : function(){
                    return false;
                }
            }
        });

    }

})();
