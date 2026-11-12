(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LigneFraisDetailController', LigneFraisDetailController);

    LigneFraisDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'LigneFrais', 'TableauFrais'];

    function LigneFraisDetailController($scope, $rootScope, $stateParams, previousState, entity, LigneFrais, TableauFrais) {
        var vm = this;

        vm.ligneFrais = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:ligneFraisUpdate', function(event, result) {
            vm.ligneFrais = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
