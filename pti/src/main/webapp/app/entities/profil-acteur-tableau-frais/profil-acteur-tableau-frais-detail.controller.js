(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ProfilActeurTableauFraisDetailController', ProfilActeurTableauFraisDetailController);

    ProfilActeurTableauFraisDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'ProfilActeurTableauFrais'];

    function ProfilActeurTableauFraisDetailController($scope, $rootScope, $stateParams, previousState, entity, ProfilActeurTableauFrais) {
        var vm = this;

        vm.profilActeurTableauFrais = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:profilActeurTableauFraisUpdate', function(event, result) {
            vm.profilActeurTableauFrais = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
