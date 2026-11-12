(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ProfilDetailController', ProfilDetailController);

    ProfilDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Profil'];

    function ProfilDetailController($scope, $rootScope, $stateParams, previousState, entity, Profil) {
        var vm = this;

        vm.profil = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:profilUpdate', function(event, result) {
            vm.profil = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
