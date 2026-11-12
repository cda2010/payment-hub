(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('relation', {
            parent: 'entity',
            url: '/relation',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'Relations'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/relation/relations.html',
                    controller: 'RelationController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
            }
        })
        .state('relation-detail', {
            parent: 'relation',
            url: '/relation/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'Relation'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/relation/relation-detail.html',
                    controller: 'RelationDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                entity: ['$stateParams', 'Relation', function($stateParams, Relation) {
                    return Relation.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'relation',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('relation-detail.edit', {
            parent: 'relation-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/relation/relation-dialog.html',
                    controller: 'RelationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Relation', function(Relation) {
                            return Relation.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('relation.new', {
            parent: 'relation',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/relation/relation-dialog.html',
                    controller: 'RelationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('relation', null, { reload: 'relation' });
                }, function() {
                    $state.go('relation');
                });
            }]
        })
        .state('relation.edit', {
            parent: 'relation',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/relation/relation-dialog.html',
                    controller: 'RelationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Relation', function(Relation) {
                            return Relation.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('relation', null, { reload: 'relation' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('relation.delete', {
            parent: 'relation',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/relation/relation-delete-dialog.html',
                    controller: 'RelationDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Relation', function(Relation) {
                            return Relation.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('relation', null, { reload: 'relation' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
