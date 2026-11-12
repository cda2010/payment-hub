(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            .state('reglement-organisation', {
            parent: 'entity',
            url: '/reglement-organisation?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.reglement-organisation.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/reglement/reglement-organisation.html',
                    controller: 'ReglementOrganisationController',
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
                    $translatePartialLoader.addPart('reglement-organisation');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
            .state('reglement-organisation-valide',{
                parent: 'entity',
                url: '/reglement-organisation-valide',
                data: {
                    authorities: ['ROLE_USER'],
                    pageTitle: 'ptiApp.reglement-organisation.home.title'
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/entities/reglement/reglement-organisation.html',
                        controller: 'ReglementOrganisationController',
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
                        $translatePartialLoader.addPart('reglement-organisation');
                        $translatePartialLoader.addPart('global');
                        return $translate.refresh();
                    }]
                }
            })
            .state('reglement-organisation-detail', {
            parent: 'reglement-organisation',
            url: '/reglement-organisation/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.reglement-organisation.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/reglement/reglement-organisation-detail.html',
                    controller: 'ReglementOrganisationDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('reglement-organisation');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'ReglementOrganisation', function($stateParams, ReglementOrganisation) {
                    return ReglementOrganisation.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'reglement-organisation',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
            .state('reglement-organisation-detail.edit', {
            parent: 'reglement-organisation-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/reglement/reglement-organisation-dialog.html',
                    controller: 'ReglementOrganisationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['ReglementOrganisation', function(ReglementOrganisation) {
                            return ReglementOrganisation.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
            .state('reglement-organisation.new', {
            parent: 'reglement-organisation',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/reglement/reglement-organisation-dialog.html',
                    controller: 'ReglementOrganisationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                dateReglement: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('reglement-organisation', null, { reload: 'reglement-organisation' });
                }, function() {
                    $state.go('reglement-organisation');
                });
            }]
        })
            .state('reglement-organisation.edit', {
            parent: 'reglement-organisation',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/reglement/reglement-organisation-dialog.html',
                    controller: 'ReglementOrganisationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['ReglementOrganisation', function(ReglementOrganisation) {
                            return ReglementOrganisation.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('reglement-organisation', null, { reload: 'reglement-organisation' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
            .state('reglement-organisation.delete', {
            parent: 'reglement-organisation',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/reglement/reglement-organisation-delete-dialog.html',
                    controller: 'ReglementDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['ReglementOrganisation', function(ReglementOrganisation) {
                            return ReglementOrganisation.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('reglement-organisation', null, { reload: 'reglement-organisation' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('reglement-template', {
            parent: 'entity',
            url: '/reglement-template?page&sort&search',
            cache: true,
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.reglement-template.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/reglement/reglement-template.html',
                    controller: 'ReglementTemplateOrganisationController',
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
                    $translatePartialLoader.addPart('reglement-organisation');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        ;
    }

})();
