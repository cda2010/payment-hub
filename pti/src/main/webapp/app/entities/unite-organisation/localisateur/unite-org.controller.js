(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LocalisateurController', LocalisateurController);

    LocalisateurController.$inject = ['$scope','RequestUtils','Ville','$state'];
    function LocalisateurController($scope,RequestUtils,Ville,$state) {
        var vm=this;
     

        //Liste des pays
        RequestUtils.GetOrganisationByType.get({ type: "PAYS" }, function(datas) {
            vm.pays = datas;
        });
       
        vm.filtre=function(){
            var itemId;

            if(vm.itemSearch.fseId!=null){
                itemId=vm.itemSearch.fseId;
            }else if(vm.itemSearch.paysId){
                itemId=vm.itemSearch.paysId;
            }else{

            }
            RequestUtils.OrgFils.query({
                uoId:itemId,
                villeId:vm.itemSearch.villeId
            }, function (datas) {
                vm.orgs = datas;
            });
        }

        vm.initFiltre=function(){
            $state.reload('localisateur');
        }
      

        $scope.$watch("vm.itemSearch.paysId", function (value) {
            if (angular.isDefined(value) && value != null) {
                Ville.query({ uoId:value}, function (datas) {
                    vm.villes = datas;
                });

                RequestUtils.GetUoFilsByType.get({
                    type: "FSE",
                    uoId: value
                }, function (datas) {
                    vm.allFse = datas;
                });
            }
        });

        $scope.$watch("vm.itemSearch.fseId", function (value) {
            if (angular.isDefined(value) && value != null) {
               
            }
        });

        $scope.$watch("vm.itemSearch.fseId", function (value) {
            if (angular.isDefined(value) && value != null) {
               
            }
        });

        

    }
})();
