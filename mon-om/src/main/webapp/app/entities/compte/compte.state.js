(function () {
    'use strict';

    angular
            .module('omnicanalApp')
            .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
                .state('compte', {
                    parent: 'entity',
                    url: '/compte/{idrelation}',
                    data: {
                        authorities: ['ROLE_USER'],
                        pageTitle: 'Comptes'
                    },
                    views: {
                        'content@app': {
                            templateUrl: 'app/entities/compte/comptes.html',
                            controller: 'CompteController',
                            controllerAs: 'vm'
                        }
                    }
                    ,
                    params: {
                        page: {
                            value: '1',
                            squash: true
                        },
                        sort: {
                            value: 'id,asc',
                            squash: true
                        },
                        search: null,
                        type: 0,
                        titulaire: 0,
                        teneur: 0
                    },
                    resolve: {
                        pagingParams: ['$stateParams', 'PaginationUtil', function ($stateParams, PaginationUtil) {
                                return {
                                    type: $stateParams.type,
                                    titulaire: $stateParams.titulaire,
                                    teneur: $stateParams.teneur,
                                    page: PaginationUtil.parsePage($stateParams.page),
                                    sort: $stateParams.sort,
                                    predicate: PaginationUtil.parsePredicate($stateParams.sort),
                                    ascending: PaginationUtil.parseAscending($stateParams.sort),
                                    search: $stateParams.search
                                };
                            }]
                    }
                })
                .state('compte-detail', {
                    parent: 'compte',
                    url: '/compte/{id}',
                    data: {
                        authorities: ['ROLE_USER'],
                        pageTitle: 'Compte'
                    },
                    views: {
                        'content@app': {
                            templateUrl: 'app/entities/compte/compte-detail.html',
                            controller: 'CompteDetailController',
                            controllerAs: 'vm'
                        }
                    },
                    resolve: {
                        entity: ['$stateParams', 'Compte', function ($stateParams, Compte) {
                                return Compte.get({id: $stateParams.id}).$promise;
                            }],
                        previousState: ["$state", function ($state) {
                                var currentStateData = {
                                    name: $state.current.name || 'compte',
                                    params: $state.params,
                                    url: $state.href($state.current.name, $state.params)
                                };
                                return currentStateData;
                            }]
                    }
                })
                .state('compte-detail.edit', {
                    parent: 'compte-detail',
                    url: '/detail/edit',
                    data: {
                        authorities: ['ROLE_USER']
                    },
                    onEnter: ['$stateParams', '$state', '$uibModal', function ($stateParams, $state, $uibModal) {
                            $uibModal.open({
                                templateUrl: 'app/entities/compte/compte-dialog.html',
                                controller: 'CompteDialogController',
                                controllerAs: 'vm',
                                backdrop: 'static',
                                size: 'lg',
                                resolve: {
                                    entity: ['Compte', function (Compte) {
                                            return Compte.get({id: $stateParams.id}).$promise;
                                        }]
                                }
                            }).result.then(function () {
                                $state.go('^', {}, {reload: false});
                            }, function () {
                                $state.go('^');
                            });
                        }]
                })
                .state('compte.new', {
                    parent: 'compte',
                    url: '/new/{type}/{teneur}/{titulaire}',
                    data: {
                        authorities: ['ROLE_USER']
                    },
                    onEnter: ['$stateParams', '$state', '$uibModal', function ($stateParams, $state, $uibModal) {

                            $uibModal.open({
                                templateUrl: 'app/entities/compte/compte-dialog.html',
                                controller: 'CompteDialogController',
                                controllerAs: 'vm',
                                backdrop: 'static',
                                size: 'lg',
                                resolve: {
                                    entity: function () {
                                        var rel;
                                        if (angular.isDefined($stateParams.idrelation) && $stateParams.idrelation !== "") {
                                            rel = {id: $stateParams.idrelation};
                                        } else {
                                            rel = null;
                                        }
                                        return {
                                            codeAgence: null,
                                            id: null,
                                            relation: rel
                                        };
                                    }
                                }
                            }).result.then(function () {
                                $state.go('compte', null, {reload: 'compte'});
                            }, function () {
                                $state.go('compte');
                            });
                        }]
                })
                .state('compte.edit', {
                    parent: 'compte',
                    url: '/{id}/edit',
                    data: {
                        authorities: ['ROLE_USER']
                    },
                    onEnter: ['$stateParams', '$state', '$uibModal', function ($stateParams, $state, $uibModal) {
                            $uibModal.open({
                                templateUrl: 'app/entities/compte/compte-dialog.html',
                                controller: 'CompteDialogController',
                                controllerAs: 'vm',
                                backdrop: 'static',
                                size: 'lg',
                                resolve: {
                                    entity: ['Compte', function (Compte) {
                                            return Compte.get({id: $stateParams.id}).$promise;
                                        }]
                                }
                            }).result.then(function () {
                                $state.go('compte', null, {reload: 'compte'});
                            }, function () {
                                $state.go('^');
                            });
                        }]
                })
                .state('compte.delete', {
                    parent: 'compte',
                    url: '/{id}/delete',
                    data: {
                        authorities: ['ROLE_USER']
                    },
                    onEnter: ['$stateParams', '$state', '$uibModal', function ($stateParams, $state, $uibModal) {
                            $uibModal.open({
                                templateUrl: 'app/entities/compte/compte-delete-dialog.html',
                                controller: 'CompteDeleteController',
                                controllerAs: 'vm',
                                size: 'md',
                                resolve: {
                                    entity: ['Compte', function (Compte) {
                                            return Compte.get({id: $stateParams.id}).$promise;
                                        }]
                                }
                            }).result.then(function () {
                                $state.go('compte', null, {reload: 'compte'});
                            }, function () {
                                $state.go('^');
                            });
                        }]
                });
    }

})();
