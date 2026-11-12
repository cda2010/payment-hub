(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('TransfertTestController', TransfertTestController);

    TransfertTestController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'InterfaceUtilisateur', 'ServiceTraitement', 'ParametreUtiliateur'];

    function TransfertTestController ($timeout, $scope, $stateParams, $uibModalInstance, entity, InterfaceUtilisateur, ServiceTraitement, ParametreUtiliateur) {
        var vm = this;
vm.send=send;

vm.essai2=essai2;
        vm.interfaceUtilisateur = entity;
        vm.clear = clear;
        vm.save = save;
          vm.liste={};
                vm.listeParam=[];
        vm.servicetraitements = ServiceTraitement.query();
     vm.essai=essai;
        vm.getParamatre=getParamatre;
        //vm.parametreutiliateurs = ParametreUtiliateur.query();
        vm.onChangeService=onChangeService;
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

   function onChangeService(service) {
          ParametreUtiliateur.queryService({idservice:service},function(result){
                 vm.parametreutiliateurs =  result;
            });
        }
function getAllElement (donnee){
    var elem = document.getElementById('editForm').elements;
for(var i = 0; i < elem.length; i++)
        {
          console.log(elem[i].name);  
             angular.forEach(donnee, function (value, key) {
                         angular.forEach(value, function (val, k){
                              if (val===elem[i].name){
                                     value.valeur=elem[i].value;
                              }
                         });
             
         });
        } 
}



function getValueOfParam(donnee, code,valeur){
    var resultat;
   
     angular.forEach(donnee, function (value, key) {
                     
         angular.forEach(value, function (val, k){
                         if (val===code){
                                      value.valeur=valeur;
                         }
                            
                     });
                    
                    
                });
            
      
}
function send (){
    vm.param=[];
           vm.listeParam=[];
     
      vm.parametre={id:1,codeParametre:'SIEGE', typeParametre:'STRING', valeur:'1', nomParametre:'', interfaceUtilisateur:{id:1},proprieteApi:'FSESOURCE'};
       vm.param.push(vm.parametre);
        vm.parametre={id:1,codeParametre:'CODEAGENCESOURCE', typeParametre:'STRING', valeur:'01001', nomParametre:'', interfaceUtilisateur:{id:1},proprieteApi:'FSESOURCE'};
       vm.param.push(vm.parametre);
        vm.parametre={id:1,codeParametre:'CODEAGENCEDEST', typeParametre:'STRING', valeur:'01001', nomParametre:'', interfaceUtilisateur:{id:1},proprieteApi:'FSESOURCE'};
       vm.param.push(vm.parametre);
        // vm.parametre={id:1,codeParametre:'CPTECLIENT', typeParametre:'STRING', valeur:'76477115', nomParametre:'', interfaceUtilisateur:{id:1},proprieteApi:'FSESOURCE'};
      //  vm.parametre={id:1,codeParametre:'CPTECLIENT', typeParametre:'STRING', valeur:'0011000001094', nomParametre:'', interfaceUtilisateur:{id:1},proprieteApi:'FSESOURCE'};
       //  vm.parametre={id:1,codeParametre:'CPTECLIENT', typeParametre:'STRING', valeur:'00033924101#01001#CI166', nomParametre:'', interfaceUtilisateur:{id:1},proprieteApi:'FSESOURCE'};
   // vm.param.push(vm.parametre);    
    vm.parametre={id:1,codeParametre:'CPTEBENEFICIAIRE', typeParametre:'STRING', valeur:'0021000000013', nomParametre:'', interfaceUtilisateur:{id:1},proprieteApi:'FSESOURCE'};
      // vm.param.push(vm.parametre);
       vm.parametre={id:1,codeParametre:'CPTECLIENT', typeParametre:'STRING', valeur:'0011000003090', nomParametre:'', interfaceUtilisateur:{id:1},proprieteApi:'FSESOURCE'};
       vm.param.push(vm.parametre);
       
         vm.parametre={id:1,codeParametre:'CAS', typeParametre:'STRING', valeur:'NORMAL', nomParametre:'CASS', interfaceUtilisateur:{id:1},proprieteApi:'FSESOURCE'};
       vm.param.push(vm.parametre);
      //  vm.parametre={id:1,codeParametre:'CPTECLIENT', typeParametre:'STRING', valeur:'02385124101#01001#BF148', nomParametre:'5035789535205252', interfaceUtilisateur:{id:1},proprieteApi:'FSESOURCE'};
    //  vm.param.push(vm.parametre);
    //   vm.parametre={id:1,codeParametre:'CPTEBEN', typeParametre:'STRING', valeur:'03273922001#01001#BF148', nomParametre:'', interfaceUtilisateur:{id:1},proprieteApi:'FSESOURCE'};
    //   vm.param.push(vm.parametre);
    //    vm.parametre={id:1,codeParametre:'CPTEOPERATIONACTEUR', typeParametre:'STRING', valeur:'00022624101#01001#BF148', nomParametre:'', interfaceUtilisateur:{id:1},proprieteApi:'FSESOURCE'};
      //vm.parametre={id:1,codeParametre:'CPTEBENEFICIAIRE', typeParametre:'STRING', valeur:'0011000001074 ', nomParametre:'', interfaceUtilisateur:{id:1},proprieteApi:'FSESOURCE'};
  //    vm.param.push(vm.parametre);
      vm.parametre={id:1,codeParametre:'DESCRIPTION', typeParametre:'STRING', valeur:'TEST', nomParametre:'', interfaceUtilisateur:{id:1},proprieteApi:'FSESOURCE'};
      vm.param.push(vm.parametre);
      
        vm.parametre={id:1,codeParametre:'REFERENCE', typeParametre:'STRING', valeur:'TEST01', nomParametre:'', interfaceUtilisateur:{id:1},proprieteApi:'FSESOURCE'};
      vm.param.push(vm.parametre);
      
   //     vm.param.push(vm.parametre);
      
        //vm.parametre={id:1,codeParametre:'CPTEOPERATIONACTEUR', typeParametre:'STRING', valeur:'00005424101#01001#CI166', nomParametre:'', interfaceUtilisateur:{id:1},proprieteApi:'FSESOURCE'};
      //  vm.param.push(vm.parametre);
    
       vm.parametre={id:1,codeParametre:'MONTANTTAXE', typeParametre:'STRING', valeur:'180', nomParametre:'', interfaceUtilisateur:{id:1},proprieteApi:'FSESOURCE'};
        vm.param.push(vm.parametre);
   
     vm.parametre={id:1,codeParametre:'CPTESRCFRAIS', typeParametre:'STRING', valeur:'76477115', nomParametre:'', interfaceUtilisateur:{id:1},proprieteApi:'FSESOURCE'};
    // vm.parametre={id:1,codeParametre:'CPTESRCFRAIS', typeParametre:'STRING', valeur:'5035789594475051', nomParametre:'', interfaceUtilisateur:{id:1},proprieteApi:'FSESOURCE'};
     vm.param.push(vm.parametre);
        
         vm.parametre={id:1,codeParametre:'MONTANTHTCOMACQ', typeParametre:'STRING', valeur:'50', nomParametre:'', interfaceUtilisateur:{id:1},proprieteApi:'FSESOURCE'};
        vm.param.push(vm.parametre);
      vm.parametre={id:1,codeParametre:'MONTANTCOMACQ', typeParametre:'STRING', valeur:'100', nomParametre:'', interfaceUtilisateur:{id:1},proprieteApi:'FSESOURCE'};
        vm.param.push(vm.parametre);
    vm.parametre={id:1,codeParametre:'MONTANTCOMM', typeParametre:'STRING', valeur:'5', nomParametre:'', interfaceUtilisateur:{id:1},proprieteApi:'FSESOURCE'};
        vm.param.push(vm.parametre);
     // vm.parametre={id:1,codeParametre:'MONTANTFRAISACQ', typeParametre:'STRING', valeur:'50', nomParametre:'', interfaceUtilisateur:{id:1},proprieteApi:'FSESOURCE'};
       // vm.param.push(vm.parametre);
    //    vm.parametre={id:1,codeParametre:'CPTEBENEFICIAIRE', typeParametre:'STRING', valeur:'00626624001#01001#BF148', nomParametre:'', interfaceUtilisateur:{id:1},proprieteApi:'FSESOURCE'};
       //  vm.parametre={id:1,codeParametre:'CPTEBENEFICIAIRE', typeParametre:'STRING', valeur:'5035789553622263', nomParametre:'', interfaceUtilisateur:{id:1},proprieteApi:'FSESOURCE'};
   //    vm.param.push(vm.parametre);
         vm.parametre={id:1,codeParametre:'MONTANT', typeParametre:'STRING', valeur:'50', nomParametre:'', interfaceUtilisateur:{id:1},proprieteApi:'FSESOURCE'};
           vm.param.push(vm.parametre);
    vm.parametre={id:1,codeParametre:'REFERENCE', typeParametre:'STRING', valeur:'XXX5445454', nomParametre:'', interfaceUtilisateur:{id:1},proprieteApi:'FSESOURCE'};
        vm.param.push(vm.parametre);
    vm.parametre={id:1,codeParametre:'MONTANTHT', typeParametre:'STRING', valeur:'50', nomParametre:'', interfaceUtilisateur:{id:1},proprieteApi:'FSESOURCE'};
        vm.param.push(vm.parametre);
      //vm.parametre={id:1,codeParametre:'MONTANTOP', typeParametre:'STRING', valeur:'42500', nomParametre:'', interfaceUtilisateur:{id:1},proprieteApi:'FSESOURCE'};
       //vm.param.push(vm.parametre);
     vm.parametre={id:1,codeParametre:'CODEFSESOURCE', typeParametre:'STRING', valeur:'0004', nomParametre:'', interfaceUtilisateur:{id:1},proprieteApi:'FSESOURCE'};  
    vm.param.push(vm.parametre);
      vm.parametre={id:1,codeParametre:'CODEFSEDEST', typeParametre:'STRING', valeur:'0004', nomParametre:'', interfaceUtilisateur:{id:1},proprieteApi:'FSESOURCE'};  
        vm.param.push(vm.parametre);
    vm.liste= {id:1,codeUniteOrganisation:'01',codeService:'011',fse:'0011',referenceExterne:'XXX5445454',proprieteApi:'FSESOURCE',param:vm.param,canal:'54545454'};
  InterfaceUtilisateur.ess( vm.liste);
}
function getParamatre(){
    vm.fseSource='0011';
    vm.fseDest='0011';
    if (vm.fseSource===vm.fseDest){
 ParametreUtiliateur.queryInterfaceService({fse:  vm.fseSource, typeService:2},function (data){
          vm.utiparam=data;
            getValueOfParam(vm.utiparam,"CODEFSESOURCE","0008");
            getValueOfParam(vm.utiparam,"CODEFSEDEST","0004");
            getAllElement(vm.utiparam);
     
      });
      
    }else{
        ParametreUtiliateur.queryInterfaceService({fse:  vm.fseSource, typeService:2},function (data){
        vm.utiparam=data;
        getValueOfParam(vm.utiparam,"CODEFSESOURCE","0008");
           
            getAllElement(vm.utiparam);
      vm.liste= {codeService:2,fse:'0008',param:vm.utiparam};
       vm.listeParam.push( vm.liste);
         ParametreUtiliateur.queryInterfaceService({fse:  vm.fseDest, typeService:2},function (data){
         vm.utiparam2=data;   
         getValueOfParam(vm.utiparam,"CODEFSEDEST","0004");
         getAllElement(vm.utiparam);
     vm.liste= {codeService:2,fse:'0004',param:vm.utiparam2};
       vm.listeParam.push( vm.liste);
       InterfaceUtilisateur.ess( vm.listeParam);
      });
       
        
      });
      
     
      
     
        
    }
}
function essai2 (){
     vm.param=[];
     vm.parametre={id:1,codeParametre:'AGENCE', typeParametre:'STRING', valeur:'01001', nomParametre:'', interfaceUtilisateur:{id:1},proprieteApi:'FSESOURCE'};
   vm.param.push(vm.parametre);
    vm.parametre={id:1,codeParametre:'NUMEROCOMPTE', typeParametre:'STRING', valeur:'30415124001', nomParametre:'', interfaceUtilisateur:{id:1},proprieteApi:'FSESOURCE'};
    vm.param.push(vm.parametre);
     vm.liste= {codeMethode:'CONSULTATIONSOLDE',fse:'0004',param:vm.param};
  InterfaceUtilisateur.ess1( vm.liste);
  
  
   
  //  vm.liste.push(vm.parametreutiliateurs);
   
}
function essai (){
   
  //  vm.liste.push(vm.parametreutiliateurs);
    InterfaceUtilisateur.presentation({codeService:"0"},0);
}

        function save () {
            vm.isSaving = true;
             InterfaceUtilisateur.presentation(vm.parametreutiliateurs, onSaveSuccess, onSaveError);
        }

        function onSaveSuccess (result) {
         //   $scope.$emit('omnicanalPresentationApp:interfaceUtilisateurUpdate', result);
            vm.resultat=(result);
            console.log(angular.fromJson(vm.resultat.responseEntity));
        //    $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
