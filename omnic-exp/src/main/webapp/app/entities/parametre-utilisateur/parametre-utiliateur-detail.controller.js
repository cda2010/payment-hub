(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('ParametreUtiliateurDetailController', ParametreUtiliateurDetailController);

    ParametreUtiliateurDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'ParametreUtiliateur', 'InterfaceUtilisateur'];

    function ParametreUtiliateurDetailController($scope, $rootScope, $stateParams, previousState, entity, ParametreUtiliateur, InterfaceUtilisateur) {
        var vm = this;

        vm.parametreUtiliateur = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('omnicanalPresentationApp:parametreUtiliateurUpdate', function(event, result) {
            vm.parametreUtiliateur = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
